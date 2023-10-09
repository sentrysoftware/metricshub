package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.strategy.utils.CollectHelper;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.HwCollectHelper;
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
		// Get the metrics hw.fan.speed and hw.fan.speed_ratio
		final Double fanSpeed = CollectHelper.getNumberMetricValue(monitor, "hw.fan.speed", false);
		final Double fanSpeedRatio = CollectHelper.getNumberMetricValue(monitor, "hw.fan.speed_ratio", false);

		// Compute the power consumption based on fanSpeed value
		if (HwCollectHelper.isValidPositive(fanSpeed)) {
			// 1000 RPM = 1 Watt
			return fanSpeed / 1000.0;
		} else {
			if (HwCollectHelper.isValidPercentage(fanSpeedRatio)) {
				// Approximately 5 Watt for 100%
				return fanSpeedRatio * 100 * 0.05;
			}
		}

		// Approximately 5 Watt for standard fan
		return fanSpeedRatio * 5;
	}

	/**
	 * Estimates the energy consumption of Fan monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			"hw.power{hw.type=\"fan\"}",
			telemetryManager.getStrategyTime()
		);
	}
}
