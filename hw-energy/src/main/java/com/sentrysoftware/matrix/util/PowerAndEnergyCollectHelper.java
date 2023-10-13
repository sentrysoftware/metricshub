package com.sentrysoftware.matrix.util;

import com.sentrysoftware.matrix.sustainability.HardwarePowerAndEnergyEstimator;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class PowerAndEnergyCollectHelper {

	/**
	 * Computes the estimated energy using two calls to power estimation then collects both power and energy in the monitor
	 *
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
}
