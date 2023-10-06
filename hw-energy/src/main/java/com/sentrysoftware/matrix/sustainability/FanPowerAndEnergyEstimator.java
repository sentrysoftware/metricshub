package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.util.CollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
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

		final Double fanSpeed = monitor.getMetric("hw.fan.speed", NumberMetric.class).getValue();

		if (CollectHelper.isValidPositive(fanSpeed)) {
			// 1000 RPM = 1 Watt
			powerConsumption = fanSpeed / 1000.0;
		} else {
			final Double fanSpeedPercent = monitor.getMetric("hw.fan.speed_ratio", NumberMetric.class).getValue();

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
