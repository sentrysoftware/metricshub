package com.sentrysoftware.hardware.agent.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.Privacy;

import net.minidev.json.parser.JSONParser;

@ExtendWith(MockitoExtension.class)
public class HostnamesDeserializerTest {
	@Mock
	private JSONParser jsonParser;

	@Test
	void testNull() throws IOException {

		assertNull(new HostnamesDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {
/**
		{
			doReturn(null).when(jsonParser).getValueAsString();
			assertThrows(IOException.class, () -> new HostnamesDeserializer().deserialize(jsonParser, null));
		}

		{
			doReturn("unknown").when(jsonParser).getValueAsString();
			assertThrows(IOException.class, () -> new HostnamesDeserializer().deserialize(jsonParser, null));
		}

		{
			doReturn("").when(jsonParser).getValueAsString();
			assertThrows(IOException.class, () -> new HostnamesDeserializer().deserialize(jsonParser, null));
		}
**/
	}
}
