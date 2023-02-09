package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public abstract class DeserializerTest implements IDeserializerTest {

	private File getTestResourceFile(String file) {
		return new File(getResourcePath() + file + RESOURCE_EXT);
	}

	@Override
	public Connector getConnector(String file) throws IOException {
		return deserializer.deserialize(getTestResourceFile(file));
	}

	protected void checkMessage(Exception e, String message) {
		assertNotNull(message, () -> "Message cannot be null.");
		assertNotEquals(MatrixConstants.EMPTY, message, () -> "Message cannot be empty.");
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

	protected void comparePreSource(final Connector connector, Map<String, Source> expected) {
		assertNotNull(connector);
		assertEquals(expected, connector.getPre());
	}
}
