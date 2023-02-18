package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;

class EmbeddedDeserializerTest extends DeserializerTest {


	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/embedded/";
	}

	@Test
	void testDeserializeTranslations() throws IOException {
		final Connector connector = getConnector("embeddedFile");
		assertNotNull(connector);
		assertEquals("embeddedFile", connector.getConnectorIdentity().getCompiledFilename());

		final Map<String, String> expected = new HashMap<>();
		expected.put("embeddedFile1","test1");
		expected.put("embeddedFile2","test2");

		assertEquals(expected, connector.getEmbedded());
	}

}