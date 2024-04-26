package org.sentrysoftware.metricshub.extension.oscommand.it.job;

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.it.job.AbstractITJob;
import org.sentrysoftware.metricshub.it.job.ITJob;

public class SuperConnectorITJob extends AbstractITJob {

	public SuperConnectorITJob(@NonNull ClientsExecutor clientsExecutor, @NonNull TelemetryManager telemetryManager) {
		super(telemetryManager);
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
