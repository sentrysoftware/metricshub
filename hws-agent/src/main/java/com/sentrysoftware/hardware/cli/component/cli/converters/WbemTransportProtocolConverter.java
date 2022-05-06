package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol.WBEMProtocols;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class WbemTransportProtocolConverter implements ITypeConverter<WbemProtocol.WBEMProtocols> {

	@Override
	public WBEMProtocols convert(final String wbemTransportProtocol) throws Exception {
		try {
			return WBEMProtocols.interpretValueOf(wbemTransportProtocol);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
