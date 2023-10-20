package com.sentrysoftware.metricshub.engine.it.job;

import java.io.IOException;
import java.nio.file.Path;

import com.sentrysoftware.metricshub.engine.it.snmp.SnmpAgent;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

import lombok.NonNull;

public class SnmpITJob extends AbstractITJob {

	public SnmpITJob(@NonNull MatsyaClientsExecutor matsyaClientsExecutor, @NonNull TelemetryManager telemetryManager) {
		super(matsyaClientsExecutor, telemetryManager);
	}

	private SnmpAgent snmpAgent;

	@Override
	public ITJob withServerRecordData(String... dataPaths) throws Exception {

		stopServer();

		snmpAgent = new SnmpAgent();

		snmpAgent.start(dataPaths);

		return this;
	}

	@Override
	public void stopServer() {
		if (isServerStarted()) {
			snmpAgent.stop();
		}
	}

	@Override
	public boolean isServerStarted() {
		return snmpAgent != null && snmpAgent.isStarted();
	}

	@Override
	public ITJob saveHostMonitoringJson(Path path) throws IOException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'saveHostMonitoringJson'");
	}

}
