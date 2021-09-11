package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;

import lombok.NonNull;
import picocli.CommandLine.ITypeConverter;

public class SnmpVersionConverter implements ITypeConverter<SNMPProtocol.SNMPVersion> {

	@Override
	public SNMPVersion convert(@NonNull final String version) throws Exception {
		return SNMPVersion.interpretValueOf(version);
	}

}
