package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol.SNMPVersion;

import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class SnmpVersionConverter implements ITypeConverter<SnmpProtocol.SNMPVersion> {

	@Override
	public SNMPVersion convert(@NonNull final String version) throws Exception {
		try {
			return SNMPVersion.interpretValueOf(version);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
