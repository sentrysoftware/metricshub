package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.INVALID_PROTOCOL_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransportProtocolsTest {
	@Test
	public void testToString() {
		final TransportProtocols transportProtocols = TransportProtocols.HTTP;
		assertEquals("http", transportProtocols.toString());
	}

	@Test
	public void testInterpretValueOf() {
		assertEquals(TransportProtocols.HTTP, TransportProtocols.interpretValueOf("http"));
		assertEquals(TransportProtocols.HTTPS, TransportProtocols.interpretValueOf("https"));
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			TransportProtocols.interpretValueOf("SFTPST");
		});
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_PROTOCOL_EXCEPTION_MESSAGE));
	}
}
