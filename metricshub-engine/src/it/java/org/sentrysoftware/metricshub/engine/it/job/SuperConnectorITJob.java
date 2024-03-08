package org.sentrysoftware.metricshub.engine.it.job;

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

public class SuperConnectorITJob extends AbstractITJob {

	public SuperConnectorITJob(@NonNull ClientsExecutor clientsExecutor, @NonNull TelemetryManager telemetryManager) {
		super(clientsExecutor, telemetryManager);
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
