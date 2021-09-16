package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol.WBEMProtocols;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class WbemTransportProtocolConverter implements ITypeConverter<WBEMProtocol.WBEMProtocols> {

	@Override
	public WBEMProtocols convert(final String wbemTransportProtocol) throws Exception {
		try {
			return WBEMProtocols.interpretValueOf(wbemTransportProtocol);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
