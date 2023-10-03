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
public class CpuPowerAndThermalCalculator {
	private TelemetryManager telemetryManager;

	static Double estimateAverageTemperature(){
		//TODO
		return null;
	}

	static Double estimateTemperatureWarningThresholdAverage(){
		//TODO
		return null;
	}

	static Double estimateThermalDissipationRate(){
		//TODO
		return null;
	}
	static Double estimateCpuPowerConsumption(){
		//TODO
		return null;
	}
}
