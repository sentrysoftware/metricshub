package com.sentrysoftware.matrix.it;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.SuperConnectorITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

class SuperConnectorIT {

	static {
		Locale.setDefault(Locale.US);
	}

	private static final String EXPECTED_PATH = "os/SuperConnector/expected.json";

	private static final String CONNECTOR_NAME = "SuperConnectorOS";

	private static final String CONNECTOR_PATH = Paths.get("src", "it", "resources", "os", "SuperConnector", CONNECTOR_NAME + ".hdfs").toAbsolutePath().toString();

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() throws Exception {

		// Compile the connector and add it to the store
		ConnectorParser connectorParser = new ConnectorParser();
		final Connector connector = connectorParser.parse(CONNECTOR_PATH);
		ConnectorStore.getInstance().getConnectors().put(CONNECTOR_NAME, connector);

		// Configure the engine
		final OsCommandConfig protocol = OsCommandConfig.builder().build();

		engineConfiguration = EngineConfiguration
			.builder()
			.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.STORAGE).build())
			.selectedConnectors(Set.of(CONNECTOR_NAME))
			.protocolConfigurations(Map.of(OsCommandConfig.class, protocol))
			.build();

	}

	@Test
	void test() throws Exception {

		final ITJob itJob = new SuperConnectorITJob();
		final IHostMonitoring hostMonitoring = new HostMonitoring();
			
		itJob
			.prepareEngine(engineConfiguration, hostMonitoring)
			.executeStrategy(new DetectionOperation())
			.executeStrategy(new DiscoveryOperation())
			.executeStrategy(new CollectOperation())
			.verifyExpected(EXPECTED_PATH);

	}
}
