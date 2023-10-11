package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.strategy.utils.CollectHelper;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoboticsPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public RoboticsPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Calculate the approximate power consumption of the media changer.<br>
	 * If it moved, 154W, if not, 48W Source:
	 * https://docs.oracle.com/en/storage/tape-storage/sl4000/slklg/calculate-total-power-consumption.html
	 *
	 * @return Double value.
	 */
	@Override
	public Double estimatePower() {
		final Double moveCount = CollectHelper.getNumberMetricValue(monitor, "hw.robotics.moves", false);
		if (moveCount != null && moveCount > 0.0) {
			return 154.0;
		}

		return 48.0;
	}

	/**
	 * Estimates the energy consumption of Robotics monitor
	 *
	 * @return Double value.
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			"hw.power{hw.type=\"robotics\"}",
			"hw.energy{hw.type=\"robotics\"}",
			telemetryManager.getStrategyTime()
		);
	}
}
