package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.Connector;

class EmbeddedFilesDeserializerTest {
 
	private static final String EMBEDDED_FILE_YAML = """
			connector:
			  detection:
			    appliesTo: [ linux ]

			embedded:
			  embedded1: test1
			  embedded2: test2
			""";

	private static ObjectMapper mapper;

	@BeforeAll
	static void setUp() {
		mapper = JsonHelper.buildYamlMapper();
	}

	@Test
	void testEmbedded() throws IOException {
		final Connector connector = JsonHelper.deserialize(mapper, new ByteArrayInputStream(EMBEDDED_FILE_YAML.getBytes()), Connector.class);
		final Map<String, String> expected = Map.of("embedded1", "test1", "embedded2", "test2");
		assertEquals(expected, connector.getEmbedded());
	}

	@Test
	void testBlankKey() throws IOException {
		// Replacer the "embedded1" key with a blank key
		String yaml = EMBEDDED_FILE_YAML.replace("embedded1", "\" \"");
		assertThrows(
			InvalidFormatException.class,
			() -> JsonHelper.deserialize(mapper, new ByteArrayInputStream(yaml.getBytes()), Connector.class)
		);
	}

	@Test
	void testNullValue() throws IOException {
		// Replacer the "test1" value with null
		String yaml = EMBEDDED_FILE_YAML.replace(" test1", "");
		assertThrows(
			InvalidFormatException.class,
			() -> JsonHelper.deserialize(mapper, new ByteArrayInputStream(yaml.getBytes()), Connector.class)
		);
	}
}
