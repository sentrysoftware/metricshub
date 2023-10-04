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
public class HostMonitorThermalCalculator {

	private TelemetryManager telemetryManager;

	/**
	 * Computes the heating margin of the host monitor
	 * @return Double
	 */
	public static Double computeHeatingMargin() {
		// TODO
		return null;
	}

	/**
	 * Computes the ambient temperature
	 * @return Double
	 */
	public static Double computeAmbientTemperature() {
		// TODO
		return null;
	}

	/**
	 * Estimates the overall average temperature
	 * @return Double
	 */
	public static Double estimateAverageTemperature() {
		//TODO
		return null;
	}

	/**
	 * Estimates temperature warning threshold average
	 * @return Double
	 */
	public static Double estimateTemperatureWarningThresholdAverage() {
		//TODO
		return null;
	}

	/**
	 * Estimates thermal dissipation rate
	 * @return Double
	 */
	public static Double estimateThermalDissipationRate() {
		//TODO
		return null;
	}
}
