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

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SNMPVersion;

@ExtendWith(MockitoExtension.class)
class SnmpVersionDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		assertNull(new SnmpVersionDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpVersionDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpVersionDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpVersionDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testV1() throws IOException {

		doReturn("v1").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V1, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("1").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V1, new SnmpVersionDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testV2() throws IOException {

		doReturn("v2").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V2C, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("2").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V2C, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("v2c").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V2C, new SnmpVersionDeserializer().deserialize(yamlParser, null));

	}

	@Test
	void testV3() throws IOException {

		doReturn("v3").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_SHA, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("v3 no auth").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_NO_AUTH, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("v3 no-auth").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_NO_AUTH, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("v3 md5").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_MD5, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("v3 sha").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_SHA, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("V3_MD5").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_MD5, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("V3_SHA").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_SHA, new SnmpVersionDeserializer().deserialize(yamlParser, null));

		doReturn("V3_NO_AUTH").when(yamlParser).getValueAsString();
		assertEquals(SNMPVersion.V3_NO_AUTH, new SnmpVersionDeserializer().deserialize(yamlParser, null));
	}
}
