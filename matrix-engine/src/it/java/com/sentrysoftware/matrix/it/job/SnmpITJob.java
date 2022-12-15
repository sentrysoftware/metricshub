package com.sentrysoftware.matrix.it.job;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.it.snmp.SnmpAgent;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SnmpITJob extends AbstractITJob {

	private SnmpAgent snmpAgent;

	@Override
	public ITJob withServerRecordData(String... dataPaths) throws Exception {

		stopServer();

		snmpAgent = new SnmpAgent();

		snmpAgent.start(dataPaths);

		return this;
	}

	@Override
	public ITJob prepareEngine(EngineConfiguration engineConfiguration, IHostMonitoring hostMonitoring) {

		// Force the agent port
		final SnmpProtocol snmpProtocol = (SnmpProtocol) engineConfiguration.getProtocolConfigurations().get(SnmpProtocol.class);
		snmpProtocol.setPort(snmpAgent.getPort());

		return super.prepareEngine(engineConfiguration, hostMonitoring);
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
