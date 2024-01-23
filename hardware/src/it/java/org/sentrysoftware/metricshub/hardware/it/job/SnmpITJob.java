package org.sentrysoftware.metricshub.hardware.it.job;

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.hardware.it.snmp.SnmpAgent;

public class SnmpITJob extends AbstractITJob {

	public SnmpITJob(@NonNull ClientsExecutor clientsExecutor, @NonNull TelemetryManager telemetryManager) {
		super(clientsExecutor, telemetryManager);
	}

	private SnmpAgent snmpAgent;

	@Override
	public ITJob withServerRecordData(String... dataPaths) throws Exception {
		stopServer();

		snmpAgent = new SnmpAgent();

		snmpAgent.start(dataPaths);

		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);

		snmpConfiguration.setPort(snmpAgent.getPort());

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
}
