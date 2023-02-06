package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimeoutDeserializerTest {

	private static final TimeoutDeserializer NON_NEGATIVE_DERSERIALIZER = new TimeoutDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {
		{
			assertNull(NON_NEGATIVE_DERSERIALIZER.deserialize(null, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedNumberIntToken();
			doReturn(0L).when(yamlParser).getValueAsLong();
			assertThrows(InvalidFormatException.class, () -> NON_NEGATIVE_DERSERIALIZER.deserialize(yamlParser, null));
		}
	}

	@Test
	void testValue() throws IOException {
		doReturn(true).when(yamlParser).isExpectedNumberIntToken();
		doReturn(1234L).when(yamlParser).getValueAsLong();
		assertEquals(1234L, NON_NEGATIVE_DERSERIALIZER.deserialize(yamlParser, null));
	}

	@Test
	void testNegativeValue() throws Exception {
		doReturn(true).when(yamlParser).isExpectedNumberIntToken();
		doReturn(-1234L).when(yamlParser).getValueAsLong();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			NON_NEGATIVE_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected InvalidFormatException to be thrown");
		} catch (InvalidFormatException e) {
			String message = "Invalid negative or zero value encountered for property 'key'.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}

	@Test
	void testZeroValue() throws Exception {
		doReturn(true).when(yamlParser).isExpectedNumberIntToken();
		doReturn(0L).when(yamlParser).getValueAsLong();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			NON_NEGATIVE_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected InvalidFormatException to be thrown");
		} catch (InvalidFormatException e) {
			String message = "Invalid negative or zero value encountered for property 'key'.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}

	@Test
	void testNotNumberIntTokenValue() throws Exception {
		doReturn(false).when(yamlParser).isExpectedNumberIntToken();
		doReturn("str").when(yamlParser).getValueAsString();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			NON_NEGATIVE_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected InvalidFormatException to be thrown");
		} catch (InvalidFormatException e) {
			String message = "Invalid value encountered for property 'key'.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}
}