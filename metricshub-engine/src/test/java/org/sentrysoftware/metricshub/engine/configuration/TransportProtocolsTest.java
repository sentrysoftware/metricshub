package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link TransportProtocols}
 */
class TransportProtocolsTest {

	@Test
	void testToString() {
		final TransportProtocols transportProtocols = TransportProtocols.HTTP;
		assertEquals("http", transportProtocols.toString());
	}

	@Test
	void testInterpretValueOf() {
		// Valid protocols are HTTP and HTTPS
		assertEquals(TransportProtocols.HTTP, TransportProtocols.interpretValueOf(Constants.HTTP));
		assertEquals(TransportProtocols.HTTPS, TransportProtocols.interpretValueOf(Constants.HTTPS));

		// Invalid protocol: throw an exception
		final Exception exception = assertThrows(
			IllegalArgumentException.class,
			() -> {
				TransportProtocols.interpretValueOf(Constants.INVALID_PROTOCOL);
			}
		);
		final String actualMessage = exception.getMessage();
		assertTrue(actualMessage.contains(Constants.INVALID_PROTOCOL_EXCEPTION_MESSAGE));
	}
}
