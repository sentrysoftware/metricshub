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
class PortDeserializerTest {

	private static final PortDeserializer PORT_DERSERIALIZER = new PortDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {
		{
			assertNull(PORT_DERSERIALIZER.deserialize(null, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedNumberIntToken();
			doReturn(0).when(yamlParser).getIntValue();
			assertThrows(InvalidFormatException.class, () -> PORT_DERSERIALIZER.deserialize(yamlParser, null));
		}
	}

	@Test
	void testValue() throws IOException {
		doReturn(true).when(yamlParser).isExpectedNumberIntToken();
		doReturn(1234).when(yamlParser).getIntValue();
		assertEquals(1234, PORT_DERSERIALIZER.deserialize(yamlParser, null));
	}

	@Test
	void testNegativeValue() throws Exception {
		doReturn(true).when(yamlParser).isExpectedNumberIntToken();
		doReturn(-1234).when(yamlParser).getIntValue();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			PORT_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected IOException to be thrown");
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
		doReturn(0).when(yamlParser).getIntValue();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			PORT_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected IOException to be thrown");
		} catch (InvalidFormatException e) {
			String message = "Invalid negative or zero value encountered for property 'key'.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}

	@Test
	void testStringValue() throws Exception {
		doReturn(false).when(yamlParser).isExpectedNumberIntToken();
		doReturn("value").when(yamlParser).getValueAsString();
		doReturn("key").when(yamlParser).getCurrentName();
		try {
			PORT_DERSERIALIZER.deserialize(yamlParser, null);
			fail("Expected IOException to be thrown");
		} catch (InvalidFormatException e) {
			String message = "Invalid value encountered for property 'key'.";
			assertTrue(
				e.getMessage().contains(message),
				() -> "Expected exception contains: " + message + ". But got: " + e.getMessage()
			);
		}
	}
}