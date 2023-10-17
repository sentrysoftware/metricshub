package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ENERGY_DISK_CONTROLLER_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_POWER_DISK_CONTROLLER_METRIC;

import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.util.HwCollectHelper;
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
	protected Double doPowerEstimation() {
		return 15.0;
	}

	/**
	 * Estimates the energy consumption of the disk controller
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			HW_POWER_DISK_CONTROLLER_METRIC,
			HW_ENERGY_DISK_CONTROLLER_METRIC,
			telemetryManager.getStrategyTime()
		);
	}
}
