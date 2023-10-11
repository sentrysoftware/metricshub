package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_TAPE_DRIVE_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_TAPE_DRIVE_METRIC;

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
public class TapeDrivePowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public TapeDrivePowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of TapeDrive monitor
	 * @return Double
	 */
	@Override
	protected Double doPowerEstimation() {
		Double mountCount = CollectHelper.getNumberMetricValue(monitor, "hw.tape_drive.operations{type=\"mount\"}", false);

		mountCount = mountCount != null ? mountCount : 0.0;

		Double unmountCount = CollectHelper.getNumberMetricValue(
			monitor,
			"hw.tape_drive.operations{type=\"unmount\"}",
			false
		);
		unmountCount = unmountCount != null ? unmountCount : 0.0;

		final boolean isActive = mountCount + unmountCount > 0;
		final String monitorName = monitor.getAttribute(MONITOR_ATTRIBUTE_NAME);
		final String lowerCaseName = monitorName == null ? EMPTY : monitorName.toLowerCase();
		return estimatePowerHelper(isActive, lowerCaseName);
	}

	/**
	 * Estimates the tape drive power consumption based on its name and its activity
	 * @param isActive      Whether the tape drive is active or not
	 * @param lowerCaseName The name of the tape drive in lower case
	 * @return double value
	 */
	double estimatePowerHelper(final boolean isActive, final String lowerCaseName) {
		if (lowerCaseName.contains("lto")) {
			return isActive ? 46 : 30;
		} else if (lowerCaseName.contains("t10000d")) {
			return isActive ? 127 : 64;
		} else if (lowerCaseName.contains("t10000")) {
			return isActive ? 93 : 61;
		} else if (lowerCaseName.contains("ts")) {
			return isActive ? 53 : 35;
		}

		return isActive ? 80 : 55;
	}

	/**
	 * Estimates the energy consumption of TapeDrive monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			HW_POWER_TAPE_DRIVE_METRIC,
			HW_ENERGY_TAPE_DRIVE_METRIC,
			telemetryManager.getStrategyTime()
		);
	}
}
