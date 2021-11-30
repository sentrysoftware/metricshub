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
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;

@ExtendWith(MockitoExtension.class)
class SnmpPrivacyDeserializerTest {

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		assertNull(new SnmpPrivacyDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {

		{
			doReturn(null).when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("unknown").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
		}

		{
			doReturn("").when(yamlParser).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
		}

	}

	@Test
	void testAes() throws IOException {
		doReturn("aes").when(yamlParser).getValueAsString();
		assertEquals(Privacy.AES, new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testDes() throws IOException {
		doReturn("des").when(yamlParser).getValueAsString();
		assertEquals(Privacy.DES, new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
	}

	@Test
	void testNoEncryption() throws IOException {

		doReturn("no").when(yamlParser).getValueAsString();
		assertEquals(Privacy.NO_ENCRYPTION, new SnmpPrivacyDeserializer().deserialize(yamlParser, null));

		doReturn("none").when(yamlParser).getValueAsString();
		assertEquals(Privacy.NO_ENCRYPTION, new SnmpPrivacyDeserializer().deserialize(yamlParser, null));
	}
}
