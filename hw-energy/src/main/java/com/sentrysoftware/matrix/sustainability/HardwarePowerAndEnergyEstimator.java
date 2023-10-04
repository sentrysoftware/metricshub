package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public abstract class HardwarePowerAndEnergyEstimator {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates the power consumption of a hardware monitor
	 * @return Double
	 */
	abstract Double estimatePower();

	/**
	 * Estimates the energy consumption of a hardware monitor
	 * @return Double
	 */
	abstract Double estimateEnergy();
}
