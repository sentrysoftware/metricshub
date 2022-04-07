package com.sentrysoftware.matrix.it;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.SuperConnectorITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

class SuperConnectorIT {

	private static final String EXPECTED_PATH = "os/superConnector/expected.json";

	private static final String CONNECTOR_NAME = "MS_HW_SuperConnectorOS";

	private static final String CONNECTOR_PATH = Paths.get("src", "it", "resources", "os", "superConnector", CONNECTOR_NAME + ".hdfs").toAbsolutePath().toString();

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() throws Exception {

		// Compile the connector and add it to the store
		ConnectorParser connectorParser = new ConnectorParser();
		final Connector connector = connectorParser.parse(CONNECTOR_PATH);
		ConnectorStore.getInstance().getConnectors().put(CONNECTOR_NAME, connector);

		// Configure the engine
		final OSCommandConfig protocol = OSCommandConfig.builder().build();

		
		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname("0.0.0.0").id("localhost").type(TargetType.STORAGE).build())
				.selectedConnectors(Set.of(CONNECTOR_NAME))
				.protocolConfigurations(Map.of(OSCommandConfig.class, protocol)).build();
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
