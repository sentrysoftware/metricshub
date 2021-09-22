package com.sentrysoftware.hardware.prometheus.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

@ExtendWith(MockitoExtension.class)
class UnkonwStatusDeserializerTest {

	private static final Optional<ParameterState> OK = Optional.of(ParameterState.OK);
	private static final Optional<ParameterState> WARN = Optional.of(ParameterState.WARN);
	private static final Optional<ParameterState> ALARM = Optional.of(ParameterState.ALARM);

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNullParser() throws IOException {

		assertEquals(Optional.empty(), new UnknownStatusDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new UnknownStatusDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("blabla").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new UnknownStatusDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testOK() throws IOException {
		doReturn("OK").when(yamlParser).getValueAsString();
		assertEquals(OK, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("0").when(yamlParser).getValueAsString();
		assertEquals(OK, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("ok").when(yamlParser).getValueAsString();
		assertEquals(OK, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("oK").when(yamlParser).getValueAsString();
		assertEquals(OK, new UnknownStatusDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testWarn() throws IOException {
		doReturn("WARN").when(yamlParser).getValueAsString();
		assertEquals(WARN, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("1").when(yamlParser).getValueAsString();
		assertEquals(WARN, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("warn").when(yamlParser).getValueAsString();
		assertEquals(WARN, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("wArN").when(yamlParser).getValueAsString();
		assertEquals(WARN, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("unknown").when(yamlParser).getValueAsString();
		assertEquals(WARN, new UnknownStatusDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testAlarm() throws IOException {
		doReturn("ALARM").when(yamlParser).getValueAsString();
		assertEquals(ALARM, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("2").when(yamlParser).getValueAsString();
		assertEquals(ALARM, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("alarm").when(yamlParser).getValueAsString();
		assertEquals(ALARM, new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("AlaRm").when(yamlParser).getValueAsString();
		assertEquals(ALARM, new UnknownStatusDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testEmpty() throws IOException {
		doReturn("").when(yamlParser).getValueAsString();
		assertEquals(Optional.empty(), new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn(" ").when(yamlParser).getValueAsString();
		assertEquals(Optional.empty(), new UnknownStatusDeserializer().deserialize(yamlParser, null));
		doReturn("	 ").when(yamlParser).getValueAsString();
		assertEquals(Optional.empty(), new UnknownStatusDeserializer().deserialize(yamlParser, null));
	}

}
