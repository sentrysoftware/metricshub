package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class HardwarePowerAndEnergyEstimator {

	protected Monitor monitor;
	protected TelemetryManager telemetryManager;

	/**
	 * Estimates the power consumption of a hardware monitor
	 * @return Double
	 */
	public abstract Double estimatePower();

	/**
	 * Estimates the energy consumption of a hardware monitor
	 * @return Double
	 */
	public abstract Double estimateEnergy();
}
