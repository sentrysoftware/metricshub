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
public class HostMonitorEnergyAndPowerEstimator {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates the power consumption of the host monitor
	 * @return Double
	 */
	public static Double computeEstimatedPower() {
		// TODO
		return null;
	}

	/**
	 * Estimates the energy consumption of the host monitor
	 * @return Double
	 */
	public static Double computeEstimatedEnergy() {
		// TODO
		return null;
	}

	/**
	 * Computes the real energy consumption of the host monitor
	 * @return Double
	 */
	public static Double computeMeasuredEnergy() {
		// TODO
		return null;
	}

	/**
	 * Computes the real power consumption of the host monitor
	 * @return Double
	 */
	public static Double computeMeasuredPower() {
		// TODO
		return null;
	}
}
