package com.sentrysoftware.matrix;

import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_DISK_CONTROLLER_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_FAN_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_NETWORK_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_ROBOTICS_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_TAPE_DRIVE_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_DISK_CONTROLLER_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_FAN_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_NETWORK_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_ROBOTICS_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_TAPE_DRIVE_METRIC;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.delegate.IPostExecutionService;
import com.sentrysoftware.matrix.strategy.utils.CollectHelper;
import com.sentrysoftware.matrix.sustainability.DiskControllerPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.FanPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.HardwarePowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.NetworkPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.RoboticsPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.TapeDrivePowerAndEnergyEstimator;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import com.sentrysoftware.matrix.util.PowerAndEnergyCollectHelper;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class HardwareEnergyPostExecutionService implements IPostExecutionService {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates and collects power and energy consumption for a given monitor type e.g: FAN, ROBOTICS, NETWORK, etc ..
	 *
	 * @param monitorType        a given monitor type {@link KnownMonitorType}
	 * @param powerMetricName    the name of the power metric of the given monitor type
	 * @param energyMetricName   the name of the energy metric of the given monitor type
	 * @param estimatorGenerator Function that generates the estimator
	 */
	private void estimateAndCollectPowerAndEnergyForMonitorType(
		final KnownMonitorType monitorType,
		final String powerMetricName,
		final String energyMetricName,
		final BiFunction<Monitor, TelemetryManager, HardwarePowerAndEnergyEstimator> estimatorGenerator
	) {
		// Find monitors having the selected monitor type
		final String monitorTypeKey = monitorType.getKey();
		final Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorTypeKey);

		// If no monitors are found, log a message
		if (sameTypeMonitors == null) {
			log.info("Host {} does not contain {} monitors", telemetryManager.getHostname(), monitorTypeKey);
			return;
		}

		// For each monitor, estimate and collect power and energy consumption metrics
		sameTypeMonitors
			.values()
			.forEach(monitor -> collectNetworkMonitorMetric(powerMetricName, energyMetricName, estimatorGenerator, monitor));
	}

	/**
	 * Runs the estimation of several metrics like power consumption,
	 * energy consumption, thermal consumption information, etc ...
	 */
	@Override
	public void run() {
		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.FAN,
			HW_POWER_FAN_METRIC,
			HW_ENERGY_FAN_METRIC,
			FanPowerAndEnergyEstimator::new
		);

		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.ROBOTICS,
			HW_POWER_ROBOTICS_METRIC,
			HW_ENERGY_ROBOTICS_METRIC,
			RoboticsPowerAndEnergyEstimator::new
		);

		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.TAPE_DRIVE,
			HW_POWER_TAPE_DRIVE_METRIC,
			HW_ENERGY_TAPE_DRIVE_METRIC,
			TapeDrivePowerAndEnergyEstimator::new
		);

		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.DISK_CONTROLLER,
			HW_POWER_DISK_CONTROLLER_METRIC,
			HW_ENERGY_DISK_CONTROLLER_METRIC,
			DiskControllerPowerAndEnergyEstimator::new
		);

		collectNetworkMetrics(
			KnownMonitorType.NETWORK,
			HW_POWER_NETWORK_METRIC,
			HW_ENERGY_NETWORK_METRIC,
			NetworkPowerAndEnergyEstimator::new
		);
	}

	/**
	 * Estimates and collects power and energy consumption for a given Network monitor type e.g: FAN, ROBOTICS, NETWORK, etc ..
	 *
	 * @param monitorType        a given monitor type {@link KnownMonitorType}
	 * @param powerMetricName    the name of the power metric of the given monitor type
	 * @param energyMetricName   the name of the energy metric of the given monitor type
	 * @param estimatorGenerator Function that generates the estimator
	 */
	private void collectNetworkMetrics(
		final KnownMonitorType monitorType,
		final String powerMetricName,
		final String energyMetricName,
		final BiFunction<Monitor, TelemetryManager, HardwarePowerAndEnergyEstimator> estimatorGenerator
	) {
		// Find monitors having the selected monitor type
		final String monitorTypeKey = monitorType.getKey();
		final Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorTypeKey);

		// If no monitors are found, log a message
		if (sameTypeMonitors == null) {
			log.info("Host {} does not contain {} monitors", telemetryManager.getHostname(), monitorTypeKey);
			return;
		}

		// For each monitor, estimate and collect power and energy consumption metrics
		sameTypeMonitors
			.values()
			.forEach(monitor -> collectNetworkMonitorMetric(powerMetricName, energyMetricName, estimatorGenerator, monitor));
	}

	/**
	 * Collect a Network Monitor bandwidthUtilization metric and estimate its power and energy consumption
	 * @param powerMetricName
	 * @param energyMetricName
	 * @param estimatorGenerator
	 * @param monitor
	 */
	private void collectNetworkMonitorMetric(
		final String powerMetricName,
		final String energyMetricName,
		final BiFunction<Monitor, TelemetryManager, HardwarePowerAndEnergyEstimator> estimatorGenerator,
		Monitor monitor
	) {
		final Double linkSpeed = CollectHelper.getNumberMetricValue(monitor, "hw.network.bandwidth.limit", false);

		// If we don't have the linkSpeed, we can't compute the bandwidthUtilizations
		if (linkSpeed != null) {
			final Double transmittedByteRate = HwCollectHelper.calculateMetricRate(
				monitor,
				"hw.network.io{direction=\"transmit\"}",
				"hw.network.bandwidth.utilization{direction=\"transmit\"}",
				telemetryManager.getHostname()
			);

			final Double receivedByteRate = HwCollectHelper.calculateMetricRate(
				monitor,
				"hw.network.io{direction=\"receive\"}",
				"hw.network.bandwidth.utilization{direction=\"receive\"}",
				telemetryManager.getHostname()
			);

			// The bandwidths are 'byteRate * 8 / linkSpeed (in Bit/s)'
			final Double bandwidthUtilizationTransmitted = HwCollectHelper.isValidPositive(transmittedByteRate)
				? transmittedByteRate * 8 / linkSpeed
				: 0.0;
			final Double bandwidthUtilizationReceived = HwCollectHelper.isValidPositive(receivedByteRate)
				? receivedByteRate * 8 / linkSpeed
				: 0.0;

			monitor.addMetric(
				"hw.network.bandwidth.utilization{direction=\"transmit\"}",
				NumberMetric
					.builder()
					.name("hw.network.bandwidth.utilization{direction=\"transmit\"}")
					.value(bandwidthUtilizationTransmitted)
					.collectTime(telemetryManager.getStrategyTime())
					.build()
			);

			monitor.addMetric(
				"hw.network.bandwidth.utilization{direction=\"receive\"}",
				NumberMetric
					.builder()
					.name("hw.network.bandwidth.utilization{direction=\"receive\"}")
					.value(bandwidthUtilizationReceived)
					.collectTime(telemetryManager.getStrategyTime())
					.build()
			);
		}

		PowerAndEnergyCollectHelper.collectPowerAndEnergy(
			monitor,
			powerMetricName,
			energyMetricName,
			telemetryManager,
			estimatorGenerator.apply(monitor, telemetryManager)
		);
	}
}
