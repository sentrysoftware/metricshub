package com.sentrysoftware.metricshub.hardware.sustainability;

import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CpuPowerEstimator extends HardwarePowerAndEnergyEstimator {

	public CpuPowerEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of the CPU
	 * @return Double
	 */
	@Override
	protected Double doPowerEstimation() {
		return null;
	}

	/**
	 * Estimates the energy consumption of the CPU
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return null;
	}
}
