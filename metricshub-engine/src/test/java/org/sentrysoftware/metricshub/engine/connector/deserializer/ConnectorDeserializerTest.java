package org.sentrysoftware.metricshub.engine.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;

class ConnectorDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/";
	}

	@Test
	void testDeserializeConnector() throws IOException {
		final Connector connector = getConnector("connector");
		final ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();

		assertNotNull(connector);
		assertEquals("testConnector", connectorIdentity.getDisplayName());
		assertEquals("testPlatforms", connectorIdentity.getPlatforms());
		assertEquals("testReliesOn", connectorIdentity.getReliesOn());
		assertEquals("1.0.0", connectorIdentity.getVersion());
		assertEquals("20", connectorIdentity.getProjectVersion());
		assertEquals("testInformation", connectorIdentity.getInformation());
	}
}
