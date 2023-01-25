package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;

class ConnectorDeserializerTest {

	@Test
	void testDeserializeDoesntThrow() throws IOException {
		final ConnectorDeserializer deserializer = new ConnectorDeserializer();
		final Connector connector = deserializer.deserialize(
			new FileInputStream("src/test/resources/test-files/connector/connector.yaml")
		);
		assertNotNull(connector);
	}

}
