package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public abstract class HardwarePowerAndEnergyEstimator {

	private Monitor monitor;
	private TelemetryManager telemetryManager;

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
