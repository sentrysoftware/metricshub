package com.sentrysoftware.matrix.it;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.SuperConnectorITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

class SuperConnectorIT {

	private static final String EXPECTED_PATH = "os/SuperConnector/expected.json";

	private static final String CONNECTOR_NAME = "SuperConnectorOS";

	private static final String CONNECTOR_PATH = Paths.get("src", "it", "resources", "os", "SuperConnector", CONNECTOR_NAME + ".hdfs").toAbsolutePath().toString();

	private static EngineConfiguration engineConfiguration;

	private static File mshwTmp;

	@BeforeAll
	static void setUp() throws Exception {

		// Compile the connector and add it to the store
		ConnectorParser connectorParser = new ConnectorParser();
		final Connector connector = connectorParser.parse(CONNECTOR_PATH);
		ConnectorStore.getInstance().getConnectors().put(CONNECTOR_NAME, connector);

		// Configure the engine
		final OsCommandConfig protocol = OsCommandConfig.builder().build();

		engineConfiguration = EngineConfiguration.builder()
				.host(HardwareHost.builder().hostname("localhost").id("localhost").type(HostType.STORAGE).build())
				.selectedConnectors(Set.of(CONNECTOR_NAME))
				.protocolConfigurations(Map.of(OsCommandConfig.class, protocol)).build();

		// Create the MSHW directory under the system's temporary directory
		// Detect windows
		if (LocalOsHandler.isWindows()) {
			mshwTmp = Files.createDirectories(new File(System.getenv("TEMP") + "/MSHW").toPath())
					.toFile();
		} else {
			// Unix will generate a folder with a random id unlike windows, so we need to
			// target this folder specifically.
			mshwTmp = Files.createDirectories(new File("/tmp/MSHW/").toPath()).toFile();
		}

		// Delete existing files, in case a previous execution of this test was brutally stopped
		Stream.of(mshwTmp.listFiles())
				.forEach(file -> assertTrue(file.delete(), "Cannot delete the file: " + file.toString()));
	}

	@AfterAll
	static void dispose() {
		// Make sure the files are removed
		Stream.of(mshwTmp.listFiles())
				.forEach(file -> assertTrue(file.delete(), "Cannot delete the file: " + file.toString()));
		// Delete the MSHW directory
		mshwTmp.delete();
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