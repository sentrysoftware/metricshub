package com.sentrysoftware.metricshub.hardware.it;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.it.job.SuperConnectorITJob;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
	private static MatsyaClientsExecutor matsyaClientsExecutor;

	@BeforeAll
	static void setUp() throws Exception {
		final OsCommandConfiguration osCommandConfiguration = OsCommandConfiguration.builder().timeout(120L).build();

		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(LOCALHOST)
			.hostname(LOCALHOST)
			.hostType(DeviceKind.STORAGE)
			.selectedConnectors(Set.of(CONNECTOR_ID))
			.configurations(Map.of(OsCommandConfiguration.class, osCommandConfiguration))
			.build();

		final ConnectorStore connectorStore = new ConnectorStore(CONNECTOR_DIRECTORY);

		telemetryManager =
			TelemetryManager.builder().connectorStore(connectorStore).hostConfiguration(hostConfiguration).build();

		matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
	}

	@Test
	void test() throws Exception {
		new SuperConnectorITJob(matsyaClientsExecutor, telemetryManager)
			.executeDiscoveryStrategy()
			.executeCollectStrategy()
			.verifyExpected("os/SuperConnectorOsIT/expected/expected.json");
	}
}
