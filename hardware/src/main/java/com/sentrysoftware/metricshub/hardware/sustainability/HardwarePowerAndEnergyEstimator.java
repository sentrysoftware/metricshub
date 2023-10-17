package com.sentrysoftware.metricshub.hardware.sustainability;

import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public abstract class HardwarePowerAndEnergyEstimator {

	@NonNull
	protected Monitor monitor;

	@NonNull
	protected TelemetryManager telemetryManager;

	protected Double estimatedPower;

	/**
	 * Estimates the power consumption of a hardware monitor then set the estimatedPower field
	 *
	 * @return Double
	 */
	public Double estimatePower() {
		estimatedPower = doPowerEstimation();
		return estimatedPower;
	}

	/**
	 * Estimates the power consumption of a hardware monitor
	 *
	 * @return Double
	 */
	protected abstract Double doPowerEstimation();

	/**
	 * Estimates the energy consumption of a hardware monitor
	 * @return Double
	 */
	public abstract Double estimateEnergy();
}
