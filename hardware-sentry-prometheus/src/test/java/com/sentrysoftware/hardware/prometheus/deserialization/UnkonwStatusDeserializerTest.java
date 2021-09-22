package com.sentrysoftware.hardware.prometheus.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

@ExtendWith(MockitoExtension.class)
class UnkonwStatusDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		assertNull(new UnkownStatusDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new UnkownStatusDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("blabla").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new UnkownStatusDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new UnkownStatusDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testOK() throws IOException {
		doReturn("OK").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.OK, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("0").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.OK, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("ok").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.OK, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("oK").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.OK, new UnkownStatusDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testWarn() throws IOException {
		doReturn("WARN").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.WARN, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("1").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.WARN, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("warn").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.WARN, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("wArN").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.WARN, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("unknown").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.WARN, new UnkownStatusDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testAlarm() throws IOException {
		doReturn("ALARM").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.ALARM, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("2").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.ALARM, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("alarm").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.ALARM, new UnkownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("AlaRm").when(yamlParser).getValueAsString();
		assertEquals(ParameterState.ALARM, new UnkownStatusDeserializer().deserialize(yamlParser, null));
	}


}
