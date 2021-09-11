package com.sentrysoftware.hardware.cli.component.cli.converters;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;

import picocli.CommandLine.ITypeConverter;

public class SnmpPrivacyConverter implements ITypeConverter<SNMPProtocol.Privacy> {

	@Override
	public Privacy convert(final String privacy) throws Exception {
		return SNMPProtocol.Privacy.interpretValueOf(privacy);
	}

}
