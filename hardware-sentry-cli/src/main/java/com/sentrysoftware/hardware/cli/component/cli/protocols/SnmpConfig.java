package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.hardware.cli.component.cli.converters.SnmpPrivacyConverter;
import com.sentrysoftware.hardware.cli.component.cli.converters.SnmpVersionConverter;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class SnmpConfig {

	@Option(
			names = "--snmp",
			defaultValue = "1",
			description = "Enables SNMP protocol version: 1, 2, 3-md5, 3-sha or 3-noauth",
			converter = SnmpVersionConverter.class
	)
	SNMPProtocol.SNMPVersion snmpVersion;

	@Option(
			names = "--snmp-port",
			defaultValue = "161",
			description = "Port of the SNMP agent (default: ${DEFAULT-VALUE})"
	)
	int port;

	@Option(
			names = "--snmp-community",
			defaultValue = "public",
			description = "Community string for SNMP version 1 and 2 (default: ${DEFAULT-VALUE})"
	)
	String community;

	@Option(
			names = "--snmp-timeout",
			defaultValue = "120",
			description = "Timeout in seconds for SNMP operations (default: ${DEFAULT-VALUE} s)"
	)
	long timeout;

	@Option(
			names = "--snmp-username",
			description = "Username for SNMP version 3 with MD5 or SHA"
	)
	String username;

	@Option(
			names = "--snmp-password",
			description = "Password for SNMP version 3 with MD5 or SHA",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

	@Option(
			names = "--snmp-privacy",
			description = "Privacy (encryption type) for SNMP version 3 (DES, AES, or none)",
			converter = SnmpPrivacyConverter.class
	)
	Privacy privacy;

	@Option(
			names = "--snmp-privacy-password",
			description = "Privacy (encryption) password",
			interactive = true,
			arity = "0..1"
	)
	char[] privacyPassword;

}

