package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;

class ExtendsDeserializerTest {

	@Test
	void testDeserializeExtends() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer
				.deserialize(new File("src/test/resources/test-files/connector/extends.yaml"));
		assertNotNull(connector);
		assertEquals("extends", connector.getConnectorIdentity().getCompiledFilename());

		var extendsConnectors = connector.getExtendsConnectors();

		assertTrue(extendsConnectors instanceof List, "extends are expected to be a List.");

		// We want to keep the order declared in the YAML file
		// Later in the post parser code, we must keep the same order to perform merge operations
		assertEquals(
			List.of("Connector1", "Connector2"),
			extendsConnectors
		);
	}

}
