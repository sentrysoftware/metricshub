package com.sentrysoftware.matrix.strategy.detection;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

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
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

class AutomaticDetectionTest {

	@Test
	public void testRunNull() {
		assertThrows(IllegalArgumentException.class, () -> new AutomaticDetection().run(null));
	}

	@Test
	public void testRunEmptyTelemetryManager() {
		TelemetryManager telemetryManager = new TelemetryManager();
		assertNull(new AutomaticDetection().run(telemetryManager));
	}

	@Test
	public void testRunExcludeAllConnectorsFiltering() {
		Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		Set<String> connectors = new HashSet<>();
		connectors.add("connector.yaml");
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		HostConfiguration hostConfiguration = new HostConfiguration("localhost", "hostId", DeviceKind.WINDOWS, 0, null, connectors, true, null, 0, null, configurations);

		File store = new File("src/test/resources/test-files/connector/detection");
		Path storePath = store.toPath();
		ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put("connector.yaml", new Connector());
		TelemetryManager telemetryManager = new TelemetryManager(monitors, hostProperties, hostConfiguration, connectorStore);
		assertEquals(new ArrayList<>(), new AutomaticDetection().run(telemetryManager));
	}

	@Test
	public void testRunAutoDetectionFiltering() {
		Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		Set<String> connectors = new HashSet<>();
		connectors.add("connector.yaml");
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		HostConfiguration hostConfiguration = new HostConfiguration("localhost", "hostId", DeviceKind.WINDOWS, 0, null, null, true, null, 0, null, configurations);

		File store = new File("src/test/resources/test-files/connector/detection");
		Path storePath = store.toPath();

		Detection detection = new Detection();
		// Disable Auto detection
		detection.setDisableAutoDetection(true);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put("connector.yaml", connector);

		TelemetryManager telemetryManager = new TelemetryManager(monitors, hostProperties, hostConfiguration, connectorStore);
		assertEquals(new ArrayList<>(), new AutomaticDetection().run(telemetryManager));
	}

	@Test
	public void testDeviceKindFiltering() {
		Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		Set<String> connectors = new HashSet<>();
		connectors.add("connector.yaml");
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		HostConfiguration hostConfiguration = new HostConfiguration("localhost", "hostId", DeviceKind.WINDOWS, 0, null, null, true, null, 0, null, configurations);

		File store = new File("src/test/resources/test-files/connector/detection");
		Path storePath = store.toPath();

		Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		// appliesTo Linux when host is Windows
		detection.setAppliesTo(Set.of(DeviceKind.LINUX));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put("connector.yaml", connector);

		TelemetryManager telemetryManager = new TelemetryManager(monitors, hostProperties, hostConfiguration, connectorStore);
		assertEquals(new ArrayList<>(), new AutomaticDetection().run(telemetryManager));
	}

	@Test
	public void testConnectionTypesFiltering() {
		Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		Set<String> connectors = new HashSet<>();
		connectors.add("connector.yaml");
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		HostConfiguration hostConfiguration = new HostConfiguration("localhost", "hostId", DeviceKind.WINDOWS, 0, null, null, true, null, 0, null, configurations);

		File store = new File("src/test/resources/test-files/connector/detection");
		Path storePath = store.toPath();

		Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		// Connection type Remote
		detection.setConnectionTypes(Set.of(ConnectionType.REMOTE));

		ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		connector.setSourceTypes(Set.of(SnmpSource.class));

		ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put("connector.yaml", connector);

		TelemetryManager telemetryManager = new TelemetryManager(monitors, hostProperties, hostConfiguration, connectorStore);
		assertEquals(new ArrayList<>(), new AutomaticDetection().run(telemetryManager));
	}

	@Test
	public void testAcceptedSourcesFiltering() {
		Map<String, Map<String, Monitor>> monitors = new HashMap<>();
		HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		Set<String> connectors = new HashSet<>();
		connectors.add("connector.yaml");
		Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());
		HostConfiguration hostConfiguration = new HostConfiguration("localhost", "hostId", DeviceKind.WINDOWS, 0, null, null, true, null, 0, null, configurations);

		File store = new File("src/test/resources/test-files/connector/detection");
		Path storePath = store.toPath();

		Detection detection = new Detection();
		detection.setDisableAutoDetection(false);
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);
		// Http Source when host is configured with Snmp
		connector.setSourceTypes(Set.of(HttpSource.class));

		ConnectorStore connectorStore = new ConnectorStore(storePath);
		connectorStore.getStore().put("connector.yaml", connector);

		TelemetryManager telemetryManager = new TelemetryManager(monitors, hostProperties, hostConfiguration, connectorStore);
		assertEquals(new ArrayList<>(), new AutomaticDetection().run(telemetryManager));
	}
}
