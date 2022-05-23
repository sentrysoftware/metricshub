package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.TransportProtocols;

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
