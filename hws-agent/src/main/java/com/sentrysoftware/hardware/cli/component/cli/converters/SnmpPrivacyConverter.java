package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.Privacy;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class SnmpPrivacyConverter implements ITypeConverter<SnmpProtocol.Privacy> {

	@Override
	public Privacy convert(final String privacy) throws Exception {
		try {
			return SnmpProtocol.Privacy.interpretValueOf(privacy);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
