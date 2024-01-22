package org.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import picocli.CommandLine.TypeConversionException;

class TransportProtocolConverterTest {

	@Test
	void testConvert() throws Exception {
		final TransportProtocolConverter transportProtocolConverter = new TransportProtocolConverter();
		final String httpTransportProtocol = "http";
		final String httpsTransportProtocol = "https";

		assertEquals(TransportProtocols.HTTP, transportProtocolConverter.convert(httpTransportProtocol));
		assertEquals(TransportProtocols.HTTPS, transportProtocolConverter.convert(httpsTransportProtocol));
		assertEquals(TransportProtocols.HTTP, transportProtocolConverter.convert(httpTransportProtocol.toUpperCase()));
		assertEquals(TransportProtocols.HTTPS, transportProtocolConverter.convert(httpsTransportProtocol.toUpperCase()));
		assertThrows(TypeConversionException.class, () -> transportProtocolConverter.convert("unknown"));
	}
}
