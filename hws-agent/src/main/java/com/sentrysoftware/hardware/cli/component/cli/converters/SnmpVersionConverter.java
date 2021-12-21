package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;

import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

public class SnmpVersionConverter implements ITypeConverter<SNMPProtocol.SNMPVersion> {

	@Override
	public SNMPVersion convert(@NonNull final String version) throws Exception {
		try {
			return SNMPVersion.interpretValueOf(version);
		} catch (IllegalArgumentException e) {
			throw new TypeConversionException(e.getMessage());
		}
	}

}
