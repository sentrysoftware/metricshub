package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.util.CollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class FanPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public FanPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of Fan monitor
	 * @return Double
	 */
	@Override
	public Double estimatePower() {
		// Approximately 5 Watt for standard fan
		double powerConsumption = 5.0;
		final Monitor monitor = super.getMonitor();

		// Get the metrics hw.fan.speed and hw.fan.speed_ratio
		final NumberMetric fanSpeedMetric = monitor.getMetric("hw.fan.speed", NumberMetric.class);
		final NumberMetric fanSpeedRatioMetric = monitor.getMetric("hw.fan.speed_ratio", NumberMetric.class);

		if (fanSpeedMetric == null && fanSpeedRatioMetric == null) {
			log.warn(
				"Could not estimate power of Fan monitor {} since hw.fan.speed and hw.fan.speed_ratio metrics are both null",
				monitor.getId()
			);
		}

		// Get the metric's value of hw.fan.speed if the metric is not null
		Double fanSpeed = null;
		if (fanSpeedMetric != null) {
			fanSpeed = fanSpeedMetric.getValue();
		}

		// Compute the power consumption based on fanSpeed value
		if (CollectHelper.isValidPositive(fanSpeed)) {
			// 1000 RPM = 1 Watt
			powerConsumption = fanSpeed / 1000.0;
		} else {
			// Get the metric's value of hw.fan.speed_ratio if the metric is not null
			Double fanSpeedPercent = null;
			if (fanSpeedRatioMetric != null) {
				fanSpeedPercent = fanSpeedRatioMetric.getValue();
			}
			if (CollectHelper.isValidPercentage(fanSpeedPercent)) {
				// Approximately 5 Watt for 100%
				powerConsumption = fanSpeedPercent * 0.05;
			}
		}
		return powerConsumption;
	}

	/**
	 * Estimates the energy consumption of Fan monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return CollectHelper.estimateEnergyUsingPower(
			getMonitor(),
			getTelemetryManager(),
			estimatedPower,
			"hw.power{hw.type=\"fan\"}",
			getTelemetryManager().getStrategyTime()
		);
	}
}
