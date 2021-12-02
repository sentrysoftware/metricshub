package com.sentrysoftware.hardware.agent.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

@ExtendWith(MockitoExtension.class)
class TimeDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void deserializeTest()  throws IOException {
		doReturn("5s").when(yamlParser).getValueAsString();
		assertEquals(5L, new TimeDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void interpretValueOfTest() {
		// null timeout.
		assertThrows(IllegalArgumentException.class, () -> TimeDeserializer.interpretValueOf(null));
		
		// wrong timeout
		assertThrows(IllegalArgumentException.class, () -> TimeDeserializer.interpretValueOf("1dy"));

		// empty timeout.
		assertEquals(0L, TimeDeserializer.interpretValueOf(""));

		// not empty timeout.
		assertEquals(34409233L, TimeDeserializer.interpretValueOf("1y4w5d6h7m5s8000ms"));
		assertEquals(31536000L, TimeDeserializer.interpretValueOf("1y"));
		assertEquals(2419200L, TimeDeserializer.interpretValueOf("4w"));
		assertEquals(432000L, TimeDeserializer.interpretValueOf("5d"));
		assertEquals(21600L, TimeDeserializer.interpretValueOf("6h"));
		assertEquals(420L, TimeDeserializer.interpretValueOf("7m"));
		assertEquals(5L, TimeDeserializer.interpretValueOf("5s"));
		assertEquals(8L, TimeDeserializer.interpretValueOf("8000ms"));
		assertEquals(34409233L, TimeDeserializer.interpretValueOf("01y04w05d06h07m05s08000ms"));
		assertEquals(120L, TimeDeserializer.interpretValueOf("120"));
	}
}
