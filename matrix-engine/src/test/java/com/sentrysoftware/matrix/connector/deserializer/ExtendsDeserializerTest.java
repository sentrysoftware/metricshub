package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.matrix.connector.model.Connector;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

class ExtendsDeserializerTest {

	@Test
	void testDeserializeExtends() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer.deserialize(
			new File("src/test/resources/test-files/extends/extends.yaml")
		);
		assertNotNull(connector);
		assertEquals("extends", connector.getConnectorIdentity().getCompiledFilename());

		var extendsConnectors = connector.getExtendsConnectors();

		assertTrue(
			extendsConnectors instanceof LinkedHashSet,
			"extends are expected to be a LinkedHashSet."
		);

		// We want to keep the order declared in the YAML file
		// Later in the post parser code, we must keep the same order to perform merge operations
		assertEquals(new LinkedHashSet<>(List.of("Connector1", "Connector2")), extendsConnectors);
	}

	@Test
	void testDeserializeExtendsNullOrEmptyConnectorRefNotAccepted() {
		{
			try {
				new ConnectorDeserializer()
					.deserialize(
						new File("src/test/resources/test-files/extends/extendsEmptyConnectorRef.yaml")
					);
				Assert.fail("Expected an IOException to be thrown.");
			} catch (IOException e) {
				String message = "The connector referenced under 'extends' cannot be null or empty.";
				assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
				);
			}
		}

		{
			try {
				new ConnectorDeserializer()
					.deserialize(
						new File("src/test/resources/test-files/extends/extendsNullConnectorRef.yaml")
					);
				Assert.fail("Expected an IOException to be thrown.");
			} catch (IOException e) {
				String message = "The connector referenced under 'extends' cannot be null or empty.";
				assertTrue(
					e.getMessage().contains(message),
					() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
				);
			}
		}
	}

	@Test
	void testDeserializeExtendsSingleEmptyValueNotAccepted() throws IOException {
		try {
			new ConnectorDeserializer()
				.deserialize(
					new File("src/test/resources/test-files/extends/extendsSingleEmptyValue.yaml")
				);

			Assert.fail("Expected an IOException to be thrown.");
		} catch (IOException e) {
			String message = "The connector referenced in 'extends' cannot be empty.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}

	@Test
	void testDeserializeExtendsSingleValueAcceptedAsLinkedHashSet() throws IOException {
		final Connector connector = new ConnectorDeserializer()
			.deserialize(new File("src/test/resources/test-files/extends/extendsSingleValue.yaml"));
		var extendsConnectors = connector.getExtendsConnectors();

		assertTrue(
			extendsConnectors instanceof LinkedHashSet,
			"extends are expected to be a LinkedHashSet."
		);

		assertEquals(new LinkedHashSet<>(List.of("Connector1")), extendsConnectors);
	}

	@Test
	void testDeserializeExtendsNullValueAcceptedAsEmptySet() throws IOException {
		final Connector connector = new ConnectorDeserializer()
			.deserialize(new File("src/test/resources/test-files/extends/extendsNullValue.yaml"));
		var extendsConnectors = connector.getExtendsConnectors();

		assertTrue(
			extendsConnectors instanceof LinkedHashSet,
			"extends are expected to be a LinkedHashSet."
		);

		assertEquals(Collections.emptySet(), extendsConnectors);
	}

	@Test
	void testDeserializeExtendsNonExistant() throws IOException {
		final Connector connector = new ConnectorDeserializer()
			.deserialize(new File("src/test/resources/test-files/extends/extendsNonExistant.yaml"));
		var extendsConnectors = connector.getExtendsConnectors();

		assertTrue(
			extendsConnectors instanceof LinkedHashSet,
			"extends are expected to be a LinkedHashSet."
		);

		assertEquals(Collections.emptySet(), extendsConnectors);
	}
}
