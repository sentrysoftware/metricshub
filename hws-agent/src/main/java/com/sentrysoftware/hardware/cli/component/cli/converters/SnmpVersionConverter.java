package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SnmpVersion;

import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class SnmpVersionConverter implements ITypeConverter<SnmpProtocol.SnmpVersion> {

	@Override
	public SnmpVersion convert(@NonNull final String version) throws Exception {
		try {
			return SnmpVersion.interpretValueOf(version);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
