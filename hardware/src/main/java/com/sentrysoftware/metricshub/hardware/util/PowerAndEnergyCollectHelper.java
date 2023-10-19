package com.sentrysoftware.metricshub.hardware.util;

import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ENERGY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_POWER;

import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.PowerMeter;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.sustainability.HardwarePowerAndEnergyEstimator;
import com.sentrysoftware.metricshub.hardware.sustainability.HostMonitorEnergyAndPowerEstimator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PowerAndEnergyCollectHelper {

	/**
	 * Computes the estimated energy using two calls to power estimation then collects both power and energy in the monitor
	 * @param monitor the monitor to collect
	 * @param powerMetricName the power metric name e.g: "hw.power{hw.type=\"fan\"}"
	 * @param energyMetricName the energy metric name e.g: "hw.energy{hw.type=\"fan\"}"
	 * @param telemetryManager the telemetry manager {@link TelemetryManager}
	 * @param hardwarePowerAndEnergyEstimator generic estimator class which can used by the different hardware {@link HardwarePowerAndEnergyEstimator}
	 */
	public static void collectPowerAndEnergy(
		final Monitor monitor,
		final String powerMetricName,
		final String energyMetricName,
		final TelemetryManager telemetryManager,
		final HardwarePowerAndEnergyEstimator hardwarePowerAndEnergyEstimator
	) {
		// Estimate power consumption
		final Double estimatedPower = hardwarePowerAndEnergyEstimator.estimatePower();
		if (estimatedPower == null) {
			log.warn(
				"Hostname {} - Received null value for power consumption. Consequently, the metric '{}' will not be collected on monitor '{}' (ID: {})",
				telemetryManager.getHostname(),
				powerMetricName,
				monitor.getType(),
				monitor.getId()
			);
			return;
		}

		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(monitor, powerMetricName, estimatedPower, telemetryManager.getStrategyTime());

		// Compute the estimated energy consumption
		final Double estimatedEnergy = hardwarePowerAndEnergyEstimator.estimateEnergy();
		if (estimatedEnergy == null) {
			log.info(
				"Hostname {} - Received null value for energy. Consequently, the metric '{}' will not be collected during this cycle on monitor '{}' (ID: {})",
				telemetryManager.getHostname(),
				energyMetricName,
				monitor.getType(),
				monitor.getId()
			);
			return;
		}

		// Collect the estimated energy consumption metric
		metricFactory.collectNumberMetric(monitor, energyMetricName, estimatedEnergy, telemetryManager.getStrategyTime());
	}

	/**
	 * Computes the estimated energy using two calls to power estimation then collects both power and energy in the host monitor
	 * @param monitor the monitor to collect
	 * @param telemetryManager the telemetry manager {@link TelemetryManager}
	 * @param hostMonitorEnergyAndPowerEstimator generic estimator class which can used by the different hardware {@link HardwarePowerAndEnergyEstimator}
	 */
	public static void collectHostPowerAndEnergy(
		final Monitor monitor,
		final TelemetryManager telemetryManager,
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator
	) {
		// Compute power consumption
		final Double computedPower;
		if (PowerMeter.MEASURED.equals(telemetryManager.getPowerMeter())) {
			computedPower = hostMonitorEnergyAndPowerEstimator.computeMeasuredPower();
		} else {
			computedPower = hostMonitorEnergyAndPowerEstimator.computeEstimatedPower();
		}

		if (computedPower == null) {
			log.warn(
				"Hostname {} - Received null value for power consumption. Consequently, the metric '{}' will not be collected on monitor '{}' (ID: {})",
				telemetryManager.getHostname(),
				HW_HOST_POWER,
				monitor.getType(),
				monitor.getId()
			);
			return;
		}

		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(monitor, HW_HOST_POWER, computedPower, telemetryManager.getStrategyTime());

		// Compute the estimated or the measured energy consumption
		final Double computedEnergy;
		if (PowerMeter.MEASURED.equals(telemetryManager.getPowerMeter())) {
			computedEnergy = hostMonitorEnergyAndPowerEstimator.computeMeasuredEnergy();
		} else {
			computedEnergy = hostMonitorEnergyAndPowerEstimator.computeEstimatedEnergy();
		}

		if (computedEnergy == null) {
			log.info(
				"Hostname {} - Received null value for energy. Consequently, the metric '{}' will not be collected during this cycle on monitor '{}' (ID: {})",
				telemetryManager.getHostname(),
				HW_HOST_ENERGY,
				monitor.getType(),
				monitor.getId()
			);
			return;
		}

		// Collect the computed energy consumption metric
		metricFactory.collectNumberMetric(monitor, HW_HOST_ENERGY, computedEnergy, telemetryManager.getStrategyTime());
	}
}
