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
public class HostMonitorEnergyEstimator {

	private TelemetryManager telemetryManager;

	static Double computeHeatingMargin() {
		// TODO
		return null;
	}

	static Double computeAmbientTemperature() {
		// TODO
		return null;
	}

	static Double computeEstimatedPower() {
		// TODO
		return null;
	}

	static Double computeEstimatedEnergy() {
		// TODO
		return null;
	}

	static Double computeMeasuredEnergy() {
		// TODO
		return null;
	}

	static Double computeMeasuredPower() {
		// TODO
		return null;
	}
}
