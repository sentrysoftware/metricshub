package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ESTIMATED_ENERGY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ESTIMATED_POWER;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_MEASURED_ENERGY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_MEASURED_POWER;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.common.helpers.NumberHelper;
import com.sentrysoftware.metricshub.engine.strategy.utils.CollectHelper;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import com.sentrysoftware.metricshub.hardware.util.HwCollectHelper;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class HostMonitorEnergyAndPowerEstimator {

	private TelemetryManager telemetryManager;
	private Monitor hostMonitor;
	private Double powerConsumption;

	private static final String HW_ENCLOSURE_POWER = "hw.enclosure.power";

	public HostMonitorEnergyAndPowerEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		this.hostMonitor = monitor;
		this.telemetryManager = telemetryManager;
	}

	/**
	 * Estimates the power consumption of the host monitor
	 * @return Double
	 */
	public Double computeEstimatedPower() {
		powerConsumption = estimateHostPowerConsumption(sumEstimatedPowerConsumptions());
		return powerConsumption;
	}

	/**
	 * Computes the measured energy consumption of the host monitor
	 * @return Double
	 */
	public Double computeMeasuredEnergy() {
		// Compute measured energy
		return HwCollectHelper.estimateEnergyUsingPower(
			hostMonitor,
			telemetryManager,
			powerConsumption,
			HW_HOST_MEASURED_POWER,
			HW_HOST_MEASURED_ENERGY,
			telemetryManager.getStrategyTime()
		);
	}

	/**
	 * Computes the estimated energy consumption of the host monitor
	 * @return Double
	 */
	public Double computeEstimatedEnergy() {
		// Compute estimated energy
		return HwCollectHelper.estimateEnergyUsingPower(
			hostMonitor,
			telemetryManager,
			powerConsumption,
			HW_HOST_ESTIMATED_POWER,
			HW_HOST_ESTIMATED_ENERGY,
			telemetryManager.getStrategyTime()
		);
	}

	/**
	 * Computes the real power consumption of the host monitor
	 * @return Double
	 */
	public Double computeMeasuredPower() {
		final Map<String, Monitor> enclosureMonitors = telemetryManager.findMonitorByType(
			KnownMonitorType.ENCLOSURE.getKey()
		);

		final Double totalMeasuredPowerConsumption = sumEnclosurePowerConsumptions(enclosureMonitors);

		// Adjust monitor power consumptions
		adjustAllPowerConsumptions(sumEstimatedPowerConsumptions(), totalMeasuredPowerConsumption);
		powerConsumption = totalMeasuredPowerConsumption;
		return powerConsumption;
	}

	/**
	 * This method checks whether a connector status was set to "ok" or "1.0"
	 * @param currentMonitor the current monitor
	 * @return boolean
	 */
	boolean isConnectorStatusOk(final Monitor currentMonitor) {
		final AbstractMetric connectorStatusMetric = currentMonitor.getMetric(CONNECTOR_STATUS_METRIC_KEY);

		if (connectorStatusMetric instanceof NumberMetric) {
			final Double connectorStatusNumberValue = CollectHelper.getNumberMetricValue(
				currentMonitor,
				CONNECTOR_STATUS_METRIC_KEY,
				false
			);
			return Double.valueOf(1.0).equals(connectorStatusNumberValue);
		} else {
			final String connectorStatusStateSetValue = CollectHelper.getStateSetMetricValue(
				currentMonitor,
				CONNECTOR_STATUS_METRIC_KEY,
				false
			);
			return "ok".equals(connectorStatusStateSetValue);
		}
	}

	/**
	 * Adjust the power consumption parameter of all the monitors
	 * @param totalEstimatedPowerConsumption Double value of the total estimated power consumption
	 * @param totalMeasuredPowerConsumption  Double value of the total measured power consumption
	 */
	void adjustAllPowerConsumptions(
		final Double totalEstimatedPowerConsumption,
		final Double totalMeasuredPowerConsumption
	) {
		final String hostname = telemetryManager.getHostname();

		if (totalEstimatedPowerConsumption == null) {
			log.debug(
				"Hostname {} - No power consumption estimated for the monitored devices. Skip power consumption adjustment.",
				hostname
			);
			return;
		}

		// Browse through all the collected objects and perform the adjustment of estimated powers
		final Stream<Monitor> monitorStream = telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> isConnectorStatusOk(monitor))
			.filter(monitor -> !KnownMonitorType.HOST.getKey().equals(monitor.getType())) // We already sum the values for the host
			.filter(monitor -> !KnownMonitorType.ENCLOSURE.getKey().equals(monitor.getType())) // Skip the enclosure
			.filter(monitor -> !KnownMonitorType.VM.getKey().equals(monitor.getType())); // Skip VM monitors as their power is already computed based on the host's power

		if (totalMeasuredPowerConsumption == null) {
			// Let's try next collect
			log.debug(
				"Hostname {} - The measured power consumption is absent." +
				" An attempt to estimate the monitors power consumption will be made during the next collect.",
				hostname
			);

			// Clear the estimated power consumption since the total measured power is not yet available.
			// This will avoid the possible estimated energy gap due to the connector that collects Energy instead of Power Consumption
			// because using the adjustment approach, on each estimated device, the first energy will be calculated based
			// on the first non-adjusted power and the second adjusted power and from collect to collect this energy gap will persist.

			monitorStream.forEach(monitor -> {
				final Map<String, AbstractMetric> metrics = monitor.getMetrics();
				final String powerMetricName = HwCollectHelper.generatePowerMetricNameForMonitorType(monitor.getType());
				final String energyMetricName = HwCollectHelper.generateEnergyMetricNameForMonitorType(monitor.getType());
				metrics.remove(powerMetricName);
				metrics.remove(energyMetricName);
			});

			return;
		}
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		monitorStream.forEach(monitor -> {
			final String powerMetricName = HwCollectHelper.generatePowerMetricNameForMonitorType(monitor.getType());
			final String energyMetricName = HwCollectHelper.generateEnergyMetricNameForMonitorType(monitor.getType());
			final Double powerMetricValue = CollectHelper.getNumberMetricValue(monitor, powerMetricName, false);
			final Double adjustedPowerValue = getAdjustedPowerConsumption(
				powerMetricValue,
				totalEstimatedPowerConsumption,
				totalMeasuredPowerConsumption
			);

			// Collect adjusted power metric
			metricFactory.collectNumberMetric(
				monitor,
				powerMetricName,
				adjustedPowerValue,
				telemetryManager.getStrategyTime()
			);

			final Double adjustedEnergyValue = HwCollectHelper.estimateEnergyUsingPower(
				monitor,
				telemetryManager,
				adjustedPowerValue,
				powerMetricName,
				energyMetricName,
				telemetryManager.getStrategyTime()
			);

			// Collect adjusted energy metric
			metricFactory.collectNumberMetric(
				monitor,
				energyMetricName,
				adjustedEnergyValue,
				telemetryManager.getStrategyTime()
			);
		});
	}

	/**
	 * This method adjusts the power consumption metric value
	 * @param estimatedPowerConsumption the estimated power consumption
	 * @param totalEstimatedPowerConsumption the total estimated power consumption
	 * @param totalMeasuredPowerConsumption the total measured power consumption
	 * @return
	 */
	Double getAdjustedPowerConsumption(
		final Double estimatedPowerConsumption,
		final Double totalEstimatedPowerConsumption,
		final Double totalMeasuredPowerConsumption
	) {
		return NumberHelper.round(
			estimatedPowerConsumption / totalEstimatedPowerConsumption * totalMeasuredPowerConsumption,
			2,
			RoundingMode.HALF_UP
		);
	}

	/**
	 * Setting the host power consumption value as the sum of all the Enclosure power consumption values.
	 * @param enclosureMonitors monitors of type {@link KnownMonitorType} = ENCLOSURE
	 * @return the sum of all enclosures power consumptions
	 */
	Double sumEnclosurePowerConsumptions(@NonNull final Map<String, Monitor> enclosureMonitors) {
		final String hostname = telemetryManager.getHostname();

		// Getting the sums of the enclosures' power consumption values
		Double totalPowerConsumption = enclosureMonitors
			.values()
			.stream()
			.filter(monitor -> isConnectorStatusOk(monitor))
			.map(monitor -> CollectHelper.getUpdatedNumberMetricValue(monitor, HW_ENCLOSURE_POWER))
			.filter(Objects::nonNull)
			.reduce(Double::sum)
			.orElse(null);

		if (totalPowerConsumption == null) {
			// Let's try next collect
			log.debug("Hostname {} - The power consumption is going to be collected during the next collect.", hostname);
			return null;
		}

		return totalPowerConsumption;
	}

	/**
	 * Perform the sum of all monitor's power consumption
	 *
	 * @return {@link Double} value. <code>null</code> if the power cannot be collected.
	 */
	Double sumEstimatedPowerConsumptions() {
		// Browse through all the collected objects and perform the sum of parameters using the map-reduce
		return telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> isConnectorStatusOk(monitor))
			.filter(monitor -> !KnownMonitorType.HOST.getKey().equals(monitor.getType())) // We already sum the values for the host
			.filter(monitor -> !KnownMonitorType.ENCLOSURE.getKey().equals(monitor.getType())) // Skip the enclosure
			.filter(monitor -> !KnownMonitorType.VM.getKey().equals(monitor.getType())) // Skip VM monitors as their power is already computed based on the host's power
			.map(monitor ->
				CollectHelper.getNumberMetricValue(
					monitor,
					HwCollectHelper.generatePowerMetricNameForMonitorType(monitor.getType()),
					false
				)
			)
			.filter(Objects::nonNull) // skip null power consumption values
			.reduce(Double::sum)
			.orElse(null);
	}

	/**
	 * Estimate the host power consumption.<br> Collects the power consumption and energy.
	 * The estimated total power consumption value is divided by 0.9 to add 10% to the final value
	 * so that we take into account the power supplies' heat dissipation (90% efficiency assumed)
	 * @param totalEstimatedPowerConsumption Sum estimated power consumptions
	 * @return the estimated power consumption of the host
	 */
	Double estimateHostPowerConsumption(final Double totalEstimatedPowerConsumption) {
		final String hostname = telemetryManager.getHostname();

		if (totalEstimatedPowerConsumption == null) {
			log.debug("Hostname {} - No power consumption estimated for the monitored devices.", hostname);
			return null;
		}

		// Add 10% because of the heat dissipation of the power supplies
		final double powerConsumption = NumberHelper.round(totalEstimatedPowerConsumption / 0.9, 2, RoundingMode.HALF_UP);
		if (powerConsumption > 0) {
			log.debug("Hostname {} - Power Consumption: Estimated at {} Watts.", hostname, powerConsumption);
		} else {
			log.warn(
				"Hostname {} - Power Consumption could not be estimated. Negative value: {}.",
				hostname,
				powerConsumption
			);
		}

		return powerConsumption;
	}
}
