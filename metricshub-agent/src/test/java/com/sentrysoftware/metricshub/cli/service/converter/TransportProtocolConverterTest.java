package com.sentrysoftware.metricshub.cli.service.converter;

import static org.junit.jupiter.api.Assertions.*;

import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import org.junit.jupiter.api.Test;
import picocli.CommandLine.TypeConversionException;

class TransportProtocolConverterTest {

	@Test
	void testConvert() throws Exception {
		final TransportProtocolConverter transportProtocolConverter = new TransportProtocolConverter();
		assertEquals(TransportProtocols.HTTP, new TransportProtocolConverter().convert("http"));
		assertThrows(TypeConversionException.class, () -> transportProtocolConverter.convert("unknown"));
	}
}
