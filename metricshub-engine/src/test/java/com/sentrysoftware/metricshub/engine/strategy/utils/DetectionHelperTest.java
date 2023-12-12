package com.sentrysoftware.metricshub.engine.strategy.utils;

import static com.sentrysoftware.metricshub.engine.strategy.utils.DetectionHelper.hasAtLeastOneTagOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectionType;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DetectionHelperTest {

	@Test
	void testHasAtLeastOneTagOf() {
		// Create a Detection object
		final Detection detection = new Detection();
		detection.setDisableAutoDetection(false);

		// Set the connector tags
		detection.setTags(Set.of("hardware"));

		// Set additional properties for the detection
		detection.setAppliesTo(Set.of(DeviceKind.WINDOWS));
		detection.setConnectionTypes(Set.of(ConnectionType.LOCAL));

		// Set the detection object in ConnectorIdentity
		final ConnectorIdentity connectorIdentity = new ConnectorIdentity();
		connectorIdentity.setDetection(detection);

		// Create a connector and set its connector identity
		final Connector connector = new Connector();
		connector.setConnectorIdentity(connectorIdentity);

		// Assertion: Connector should have at least one of the specified tags
		assertTrue(hasAtLeastOneTagOf(Set.of("hardware", "storage"), connector));

		// Assertion: Connector should not have any of the specified tags
		assertFalse(hasAtLeastOneTagOf(Set.of("Unix", "AIX"), connector));

		// Assertion: hasAtLeastOneTagOf considers that the connector should be kept
		// Due to null or empty "includeConnectorTags"
		assertTrue(hasAtLeastOneTagOf(Collections.emptySet(), connector));
		assertTrue(hasAtLeastOneTagOf(null, connector));

		// Modify detection tags to be empty
		detection.setTags(Collections.emptySet());

		// Assertion: Connector should not have any of the specified tags
		assertFalse(hasAtLeastOneTagOf(Set.of("hardware", "AIX"), connector));

		// Modify detection tags to be null
		detection.setTags(null);

		// Assertion: Connector should not have any of the specified tags
		assertFalse(hasAtLeastOneTagOf(Set.of("hardware"), connector));
	}
}
