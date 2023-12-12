package com.sentrysoftware.metricshub.engine.strategy.utils;

import static com.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static com.sentrysoftware.metricshub.engine.strategy.utils.DetectionHelper.hasAtLeastOneTagOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectionType;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DetectionHelperTest {

	@Test
	void testRunIncludeConnectorTags() {
		// Set the host to be a local host
		final HostProperties hostProperties = new HostProperties();
		hostProperties.setLocalhost(true);

		// Create the snmp configuration
		final Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();
		configurations.put(SnmpConfiguration.class, new SnmpConfiguration());

		// Set includeSelectedTags in HostConfiguration
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
			configurations,
			null,
			Set.of("hardware", "storage")
		);

		// Create a Detection object
		final Detection detection = new Detection();
		detection.setDisableAutoDetection(false);

		// Set the connector tags
		detection.setTags(Set.of("hardware"));

		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		// Set the detection object in ConnectorIdentity
		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		// Create a connector and set its connector identity
		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);

		// Set the connector source types
		connector.setSourceTypes(Set.of(SnmpTableSource.class));

		// Check the result of method "DetectionHelper.hasAtLeastOneTagOf"
		assertTrue(hasAtLeastOneTagOf(hostConfiguration.getIncludeConnectorTags(), connector));

		// Set includeSelectedTags in HostConfiguration to another value and check the result of method "hasAtLeastOneTagOf"
		hostConfiguration.setIncludeConnectorTags(Set.of("Unix", "AIX"));
		assertFalse(hasAtLeastOneTagOf(hostConfiguration.getIncludeConnectorTags(), connector));

		// Set an empty includeSelectedTags value in HostConfiguration, the connector tags remains not empty
		hostConfiguration.setIncludeConnectorTags(Collections.emptySet());
		assertTrue(hasAtLeastOneTagOf(hostConfiguration.getIncludeConnectorTags(), connector));

		// Set an empty connector tags value with a not empty includeSelectedTags value in HostConfiguration
		hostConfiguration.setIncludeConnectorTags(Set.of("hardware", "AIX"));
		detection.setTags(Collections.emptySet());
		assertFalse(hasAtLeastOneTagOf(hostConfiguration.getIncludeConnectorTags(), connector));

		// Make connector tags value empty with an empty includeSelectedTags value in HostConfiguration
		hostConfiguration.setIncludeConnectorTags(Collections.emptySet());
		detection.setTags(Collections.emptySet());
		assertTrue(hasAtLeastOneTagOf(hostConfiguration.getIncludeConnectorTags(), connector));
	}
}
