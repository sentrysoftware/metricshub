package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DiskControllerPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public DiskControllerPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Sets the power consumption (15W by default for disk controllers)
	 * (Source: https://forums.servethehome.com/index.php?threads/raid-controllers-power-consumption.9189/)
	 * @return Double
	 *
	 */
	@Override
	public Double estimatePower() {
		return 15.0;
	}

	/**
	 * Estimates the energy consumption of the disk controller
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			"hw.power{hw.type=\"disk_controller\"}",
			"hw.energy{hw.type=\"disk_controller\"}",
			telemetryManager.getStrategyTime()
		);
	}
}
