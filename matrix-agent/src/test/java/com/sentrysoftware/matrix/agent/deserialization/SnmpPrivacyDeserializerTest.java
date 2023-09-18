package com.sentrysoftware.matrix.agent.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration.Privacy;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnmpPrivacyDeserializerTest {

	@Mock
	private YAMLParser yamlParserMock;

	@Test
	void testNull() throws IOException {
		assertNull(new SnmpPrivacyDeserializer().deserialize(null, null));
	}

	@Test
	void testBadValue() throws IOException {
		{
			doReturn(null).when(yamlParserMock).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
		}

		{
			doReturn("unknown").when(yamlParserMock).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
		}

		{
			doReturn("").when(yamlParserMock).getValueAsString();
			assertThrows(IOException.class, () -> new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
		}
	}

	@Test
	void testAes() throws IOException {
		doReturn("aes").when(yamlParserMock).getValueAsString();
		assertEquals(Privacy.AES, new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
	}

	@Test
	void testDes() throws IOException {
		doReturn("des").when(yamlParserMock).getValueAsString();
		assertEquals(Privacy.DES, new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
	}

	@Test
	void testNoEncryption() throws IOException {
		doReturn("no").when(yamlParserMock).getValueAsString();
		assertEquals(Privacy.NO_ENCRYPTION, new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));

		doReturn("none").when(yamlParserMock).getValueAsString();
		assertEquals(Privacy.NO_ENCRYPTION, new SnmpPrivacyDeserializer().deserialize(yamlParserMock, null));
	}
}
