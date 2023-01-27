package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;

class ConnectorDeserializerTest {

	@Test
	void testDeserializeConnector() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer.deserialize(
			new File("src/test/resources/test-files/connector/connector.yaml")
		);
		final ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();

		assertNotNull(connector);
		assertEquals("connector", connectorIdentity.getCompiledFilename());
		assertEquals("testConnector", connectorIdentity.getDisplayName());
		assertEquals("testPlatforms", connectorIdentity.getPlatforms());
		assertEquals("testReliesOn", connectorIdentity.getReliesOn());
		assertEquals("1.0.0", connectorIdentity.getVersion());
		assertEquals("20", connectorIdentity.getProjectVersion());
		assertEquals("testInformation", connectorIdentity.getInformation());
	}
}
