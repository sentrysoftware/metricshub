package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol.WbemProtocols;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class WbemTransportProtocolConverter implements ITypeConverter<WbemProtocol.WbemProtocols> {

	@Override
	public WbemProtocols convert(final String wbemTransportProtocol) throws Exception {
		try {
			return WbemProtocols.interpretValueOf(wbemTransportProtocol);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
