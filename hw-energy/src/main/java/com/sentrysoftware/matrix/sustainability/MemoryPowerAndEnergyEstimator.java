package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
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
	 * @return Double
	 */
	@Override
	public Double estimatePower() {
		return null;
	}

	/**
	 * Estimates the energy consumption of the Memory monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return null;
	}
}
