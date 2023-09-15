package com.sentrysoftware.matrix.strategy.detection;

import static com.sentrysoftware.matrix.constants.Constants.CONNECTOR_YAML;
import static com.sentrysoftware.matrix.constants.Constants.DETECTION_FOLDER;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.identity.ConnectionType;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.identity.Detection;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AutomaticDetectionTest {

	@Test
	void testRunNull() {
		final TelemetryManager telemetryManager = new TelemetryManager();
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertThrows(IllegalArgumentException.class, () -> new AutomaticDetection(null, matsyaClientsExecutor));
		assertThrows(IllegalArgumentException.class, () -> new AutomaticDetection(telemetryManager, null));
	}

	@Test
	void testRunEmptyTelemetryManager() {
		final TelemetryManager telemetryManager = new TelemetryManager();
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(Collections.emptyList(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testRunExcludeAllConnectorsFiltering() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final Set<String> connectors = new HashSet<>();
		connectors.add(CONNECTOR_YAML);
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		HostConfiguration hostConfiguration = new HostConfiguration(
			LOCALHOST,
			"hostId",
			DeviceKind.WINDOWS,
			0,
			null,
			connectors,
			true,
			null,
			0,
			null,
			configurations
		);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();
		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, new Connector());
		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(new ArrayList<>(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testRunAutoDetectionFiltering() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final Set<String> connectors = new HashSet<>();
		connectors.add(CONNECTOR_YAML);
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		final HostConfiguration hostConfiguration = new HostConfiguration(
			LOCALHOST,
			"hostId",
			DeviceKind.WINDOWS,
			0,
			null,
			null,
			true,
			null,
			0,
			null,
			configurations
		);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final Detection detection = new Detection();
		// Disable Auto detection
		detection.setDisableAutoDetection(true);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(new ArrayList<>(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testDeviceKindFiltering() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final Set<String> connectors = new HashSet<>();
		connectors.add(CONNECTOR_YAML);
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		final HostConfiguration hostConfiguration = new HostConfiguration(
			LOCALHOST,
			"hostId",
			DeviceKind.WINDOWS,
			0,
			null,
			null,
			true,
			null,
			0,
			null,
			configurations
		);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		// appliesTo Linux when host is Windows
		detection.setAppliesTo(Set.of(DeviceKind.LINUX));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(new ArrayList<>(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testConnectionTypesFiltering() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final Set<String> connectors = new HashSet<>();
		connectors.add(CONNECTOR_YAML);
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		final HostConfiguration hostConfiguration = new HostConfiguration(
			LOCALHOST,
			"hostId",
			DeviceKind.WINDOWS,
			0,
			null,
			null,
			true,
			null,
			0,
			null,
			configurations
		);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		// Connection type Remote
		detection.setConnectionTypes(Set.of(ConnectionType.REMOTE));

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(new ArrayList<>(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}

	@Test
	void testAcceptedSourcesFiltering() {
		final Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		final Set<String> connectors = new HashSet<>();
		connectors.add(CONNECTOR_YAML);
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		final HostConfiguration hostConfiguration = new HostConfiguration(
			LOCALHOST,
			"hostId",
			DeviceKind.WINDOWS,
			0,
			null,
			null,
			true,
			null,
			0,
			null,
			configurations
		);

		final File store = new File(DETECTION_FOLDER);
		final Path storePath = store.toPath();

		final Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		// Http Source when host is configured with Snmp
		connector.setSourceTypes(Set.of(HttpSource.class));

		final ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put(CONNECTOR_YAML, connector);

		final TelemetryManager telemetryManager = new TelemetryManager(
			monitors,
			hostProperties,
			hostConfiguration,
			connectorStore
		);
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);
		assertEquals(new ArrayList<>(), new AutomaticDetection(telemetryManager, matsyaClientsExecutor).run());
	}
}
