package com.sentrysoftware.metricshub.cli.service;

import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.io.PrintWriter;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class PrettyPrinterService {

	@NonNull
	private TelemetryManager telemetryManager;

	@NonNull
	private PrintWriter printWriter;

	/**
	 * Print the current {@link TelemetryManager} result in a human-readable way.
	 */
	public void print() {
		printWriter.println(telemetryManager.toJson());
	}
}
