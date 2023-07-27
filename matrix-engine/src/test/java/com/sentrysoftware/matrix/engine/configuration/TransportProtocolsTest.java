package com.sentrysoftware.matrix.engine.configuration;

import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.HTTP;
import static com.sentrysoftware.matrix.constants.Constants.HTTPS;
import static com.sentrysoftware.matrix.constants.Constants.INVALID_PROTOCOL;
import static com.sentrysoftware.matrix.constants.Constants.INVALID_PROTOCOL_EXCEPTION_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test of {@link TransportProtocols}
 */
public class TransportProtocolsTest {
	@Test
	public void testToString() {
		final TransportProtocols transportProtocols = TransportProtocols.HTTP;
		assertEquals("http", transportProtocols.toString());
	}

	@Test
	public void testInterpretValueOf() {
		// Valid protocols are HTTP and HTTPS
		assertEquals(TransportProtocols.HTTP, TransportProtocols.interpretValueOf(HTTP));
		assertEquals(TransportProtocols.HTTPS, TransportProtocols.interpretValueOf(HTTPS));

		// Invalid protocol: throw an exception
		final Exception exception = assertThrows(IllegalArgumentException.class, () -> {
			TransportProtocols.interpretValueOf(INVALID_PROTOCOL);
		});
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(INVALID_PROTOCOL_EXCEPTION_MESSAGE));
	}
}
