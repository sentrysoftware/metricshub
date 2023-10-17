package com.sentrysoftware.metricshub.engine.strategy.detection;

import static com.sentrysoftware.metricshub.engine.constants.Constants.CONNECTOR_YAML;
import static com.sentrysoftware.metricshub.engine.constants.Constants.DETECTION_FOLDER;
import static com.sentrysoftware.metricshub.engine.constants.Constants.STRATEGY_TIME;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConnectorSelectionTest {

	@Test
	void testRunNull() {
		final TelemetryManager telemetryManager = new TelemetryManager();
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertThrows(IllegalArgumentException.class, () -> new ConnectorSelection(null, matsyaClientsExecutor));
		assertThrows(IllegalArgumentException.class, () -> new ConnectorSelection(telemetryManager, null));
	}

	@Test
	void testRunEmptyTelemetryManager() {
		TelemetryManager telemetryManager = new TelemetryManager();
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(Collections.emptyList(), new ConnectorSelection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testRunNoSelectedConnectors() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname("localhost");
		assertNull(hostConfiguration.getSelectedConnectors());

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(new Detection());

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore,
			STRATEGY_TIME
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(Collections.emptyList(), new ConnectorSelection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testRunEmptySelectedConnectors() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname("localhost");
		// Empty selected connector set
		hostConfiguration.setSelectedConnectors(new HashSet<>());

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(new Detection());

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore,
			STRATEGY_TIME
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(Collections.emptyList(), new ConnectorSelection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testRunConnectorNotSelected() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final HostConfiguration hostConfiguration = new HostConfiguration();
		hostConfiguration.setHostname("localhost");
		hostConfiguration.setSelectedConnectors(Collections.singleton(CONNECTOR_YAML));

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(new Detection());

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		// Connector not in the selected connectors set
		connectorStore.getStore().put("connector2.yaml", connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore,
			STRATEGY_TIME
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);

		assertEquals(new ArrayList<>(), new ConnectorSelection(telemetryManager, matsyaClientsExecutor).run());
	}
}
