package org.sentrysoftware.metricshub.extension.snmp.it.job;

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.snmp.SnmpConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.it.job.snmp4j.SnmpAgent;
import org.sentrysoftware.metricshub.it.job.AbstractITJob;
import org.sentrysoftware.metricshub.it.job.ITJob;

public class SnmpITJob extends AbstractITJob {

	public SnmpITJob(@NonNull TelemetryManager telemetryManager) {
		super(telemetryManager);
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
