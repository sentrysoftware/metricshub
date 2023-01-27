package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;

class ConnectorDeserializerTest {

	@Test
	void testDeserializeDoesntThrow() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer.deserialize(
			new File("src/test/resources/test-files/connector/connector.yaml")
		);

		assertNotNull(connector);
		assertEquals("connector", connector.getConnectorIdentity().getCompiledFilename());
		assertEquals("testConnector", connector.getConnectorIdentity().getDisplayName());
		assertEquals("testPlatforms", connector.getConnectorIdentity().getPlatforms());
		assertEquals("testReliesOn", connector.getConnectorIdentity().getReliesOn());
		assertEquals("1.0.0", connector.getConnectorIdentity().getVersion());
		assertEquals("20", connector.getConnectorIdentity().getProjectVersion());
		assertEquals("testInformation", connector.getConnectorIdentity().getInformation());
	}
}
