package com.sentrysoftware.matrix.connector.deserializer;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

abstract class DeserializerTest implements IDeserializerTest {

	private File getTestResourceFile(String file) {
		return new File(getResourcePath() + file + RESOURCE_EXT);
	}

	@Override
	public Connector getConnector(String file) throws IOException {
		return deserializer.deserialize(getTestResourceFile(file));
	}

	protected void checkMessage(Exception e, String message) {
		assertTrue(
				e.getMessage().contains(message),
				() -> "Exception expected to contain: " + message + ". But got: " + e.getMessage());
	}

	protected void compareCriterion(String testResource, final Connector connector, List<Criterion> expected) {
		assertNotNull(connector);

		final ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();
		assertEquals(testResource, connectorIdentity.getCompiledFilename());

		assertNotNull(connectorIdentity.getDetection());
		assertEquals(expected, connectorIdentity.getDetection().getCriteria());
	}
}
