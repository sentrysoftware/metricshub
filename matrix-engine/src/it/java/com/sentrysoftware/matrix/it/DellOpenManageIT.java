package com.sentrysoftware.matrix.it;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.SnmpITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

class DellOpenManageIT {

	private static final String CONNECTOR_NAME = "DellOpenManage";
	private static final String CONNECTOR_PATH = Paths.get("src", "it", "resources", "snmp", "DellOpenManage", CONNECTOR_NAME + ".hdfs").toAbsolutePath().toString();

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() throws Exception {

		// Compile the connector and add it to the store
		ConnectorParser connectorParser = new ConnectorParser();
		final Connector connector = connectorParser.parse(CONNECTOR_PATH);
		ConnectorStore.getInstance().getConnectors().put(CONNECTOR_NAME, connector);

		// Configure the engine
		final SnmpProtocol protocol = SnmpProtocol
			.builder()
			.community("public")
			.version(SnmpVersion.V1)
			.timeout(120L)
			.build();

		engineConfiguration = EngineConfiguration
			.builder()
			.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.LINUX).build())
			.selectedConnectors(Set.of(CONNECTOR_NAME))
			.protocolConfigurations(Map.of(SnmpProtocol.class, protocol))
			.build();

	}

	@Test
	void test() throws Exception {

		final ITJob itJob = new SnmpITJob();
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		itJob
			.withServerRecordData("snmp/DellOpenManage/input/input.snmp")
			.prepareEngine(engineConfiguration, hostMonitoring)
			.executeStrategy(new DetectionOperation())
			.executeStrategy(new DiscoveryOperation())
			.executeStrategy(new CollectOperation())
			.verifyExpected("snmp/DellOpenManage/expected/expected.json");
	}
}
