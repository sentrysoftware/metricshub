package org.sentrysoftware.metricshub.engine.it;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.OsCommandTestConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.it.job.SuperConnectorITJob;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

class SuperConnectorOsIT {
	static {
		Locale.setDefault(Locale.US);
	}

	private static final String CONNECTOR_ID = "SuperConnectorOs";
	private static final Path CONNECTOR_DIRECTORY = Paths.get(
		"src",
		"it",
		"resources",
		"os",
		"SuperConnectorOsIT",
		"connectors"
	);
	private static final String LOCALHOST = "localhost";

	private static TelemetryManager telemetryManager;
	private static ClientsExecutor clientsExecutor;

	@BeforeAll
	static void setUp() throws Exception {
		final OsCommandTestConfiguration osCommandConfiguration = OsCommandTestConfiguration
			.builder()
			.timeout(120L)
			.build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(LOCALHOST)
			.hostname(LOCALHOST)
			.hostType(DeviceKind.STORAGE)
			.connectors(Set.of("+" + CONNECTOR_ID))
			.configurations(Map.of(OsCommandTestConfiguration.class, osCommandConfiguration))
			.build();

		final ConnectorStore connectorStore = new ConnectorStore(CONNECTOR_DIRECTORY);

		telemetryManager =
			TelemetryManager.builder().connectorStore(connectorStore).hostConfiguration(hostConfiguration).build();

		clientsExecutor = new ClientsExecutor(telemetryManager);
	}

	@Test
	void test() throws Exception {
		new SuperConnectorITJob(clientsExecutor, telemetryManager)
			.executeDiscoveryStrategy()
			.executeCollectStrategy()
			.verifyExpected("os/SuperConnectorOsIT/expected/expected.json");
	}
}
