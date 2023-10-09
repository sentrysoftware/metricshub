package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HostMonitorEnergyAndPowerEstimator {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates the power consumption of the host monitor
	 * @return Double
	 */
	public static Double computeEstimatedPower() {
		return null;
	}

	/**
	 * Estimates the energy consumption of the host monitor
	 * @return Double
	 */
	public static Double computeEstimatedEnergy() {
		return null;
	}

	/**
	 * Computes the real energy consumption of the host monitor
	 * @return Double
	 */
	public static Double computeMeasuredEnergy() {
		return null;
	}

	/**
	 * Computes the real power consumption of the host monitor
	 * @return Double
	 */
	public static Double computeMeasuredPower() {
		return null;
	}
}
