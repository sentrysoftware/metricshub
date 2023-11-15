package com.sentrysoftware.metricshub.cli.service.converter;

import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class TransportProtocolConverter implements ITypeConverter<TransportProtocols> {

	@Override
	public TransportProtocols convert(final String transportProtocol) throws Exception {
		try {
			return TransportProtocols.interpretValueOf(transportProtocol);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
