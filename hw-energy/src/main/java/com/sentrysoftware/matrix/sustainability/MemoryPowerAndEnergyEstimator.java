package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.util.HwConstants.HW_ENERGY_MEMORY_METRIC;
import static com.sentrysoftware.matrix.util.HwConstants.HW_POWER_MEMORY_METRIC;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MemoryPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public MemoryPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of the Memory monitor
	 *
	 * @return Double
	 */
	@Override
	protected Double doPowerEstimation() {
		// The power consumption of a memory module is always assumed to be 4 watts.
		return 4.0;
	}

	/**
	 * Estimates the energy consumption of the Memory monitor
	 *
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			HW_POWER_MEMORY_METRIC,
			HW_ENERGY_MEMORY_METRIC,
			telemetryManager.getStrategyTime()
		);
	}
}
