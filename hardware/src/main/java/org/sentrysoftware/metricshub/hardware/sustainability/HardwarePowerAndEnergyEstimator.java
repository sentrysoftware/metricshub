package org.sentrysoftware.metricshub.hardware.sustainability;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

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
