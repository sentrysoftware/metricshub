package com.sentrysoftware.matrix.it;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol.WbemProtocols;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.WbemITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

class EMCDiskArrayIT {

	private static final String INPUT_PATH = Paths.get("src", "it", "resources", "wbem", "emcDiskArray", "input").toAbsolutePath().toString();

	private static final String CONNECTOR_NAME = "EMCDiskArray";

	private static final String CONNECTOR_PATH = Paths.get("src", "it", "resources", "wbem", "emcDiskArray", CONNECTOR_NAME + ".hdfs").toAbsolutePath().toString();

	private static final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> PROTOCOL_CONFIGURATIONS = Map.of(
			WbemProtocol.class,
			WbemProtocol.builder()
				.protocol(WbemProtocols.HTTPS)
				.port(5900)
				.username("username")
				.password("password".toCharArray())
				.timeout(120L)
				.build());

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() throws Exception {

		// Compile the connector and add it to the store
		ConnectorParser connectorParser = new ConnectorParser();
		final Connector connector = connectorParser.parse(CONNECTOR_PATH);
		ConnectorStore.getInstance().getConnectors().put(CONNECTOR_NAME, connector);

		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname("0.0.0.0").id("localhost").type(TargetType.STORAGE).build())
				.selectedConnectors(Set.of(CONNECTOR_NAME))
				.protocolConfigurations(PROTOCOL_CONFIGURATIONS).build();
	}

	@Test
	void test() throws Exception {

		final ITJob itJob = new WbemITJob();
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		itJob
			.withServerRecordData(INPUT_PATH)
			.prepareEngine(engineConfiguration, hostMonitoring)
			.executeStrategy(new DetectionOperation())
			.executeStrategy(new DiscoveryOperation())
			.executeStrategy(new CollectOperation())
			.verifyExpected("wbem/emcDiskArray/expected/expected.json");
	}
}
