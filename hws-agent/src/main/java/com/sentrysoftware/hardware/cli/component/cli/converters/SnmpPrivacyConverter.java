package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class SnmpPrivacyConverter implements ITypeConverter<SNMPProtocol.Privacy> {

	@Override
	public Privacy convert(final String privacy) throws Exception {
		try {
			return SNMPProtocol.Privacy.interpretValueOf(privacy);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
