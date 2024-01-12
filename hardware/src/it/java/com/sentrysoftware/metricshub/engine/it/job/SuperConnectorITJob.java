package com.sentrysoftware.metricshub.engine.it.job;

import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import lombok.NonNull;

public class SuperConnectorITJob extends AbstractITJob {

	public SuperConnectorITJob(
		@NonNull MatsyaClientsExecutor matsyaClientsExecutor,
		@NonNull TelemetryManager telemetryManager
	) {
		super(matsyaClientsExecutor, telemetryManager);
	}

	@Override
	public ITJob withServerRecordData(String... recordDataPaths) throws Exception {
		return this;
	}

	@Override
	public void stopServer() {
		// There is no server to stop
	}

	@Override
	public boolean isServerStarted() {
		// We don't really have a server but let's say server is simulated as started for the SuperConnector
		// Knowing that it only perform local OS commands and AWK calls
		return true;
	}
}
