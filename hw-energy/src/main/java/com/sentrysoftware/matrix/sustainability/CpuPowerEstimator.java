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
public class CpuPowerEstimator {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates the power consumption of the CPU
	 * @return Double
	 */
	public static Double estimatePower() {
		//TODO
		return null;
	}
}
