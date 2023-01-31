package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;

@ExtendWith(MockitoExtension.class)
class ResultContentDeserializerTest {

	private static final ResultContentDeserializer RESULT_CONTENT_DESERIALIZER = new ResultContentDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertNull(RESULT_CONTENT_DESERIALIZER.deserialize(null, null));
		}

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertNull(RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		}

	}

	@Test
	void testBadValue() throws IOException {
		{
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		}

	}

	@Test
	void testDeserialize() throws IOException {
		doReturn("httpStatus").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HTTP_STATUS, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("http_status").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HTTP_STATUS, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("HTTP_STATUS").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HTTP_STATUS, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("body").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.BODY, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("Body").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.BODY, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("BODY").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.BODY, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("header").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HEADER, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("Header").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HEADER, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("HEADER").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.HEADER, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("all").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.ALL, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("All").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.ALL, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
		doReturn("ALL").when(yamlParser).getValueAsString();
		assertEquals(ResultContent.ALL, RESULT_CONTENT_DESERIALIZER.deserialize(yamlParser, null));
	}
}