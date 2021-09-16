package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.hardware.cli.component.cli.converters.SnmpPrivacyConverter;
import com.sentrysoftware.hardware.cli.component.cli.converters.SnmpVersionConverter;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class SnmpConfig implements IProtocolConfig {

	@Option(
			names = "--snmp",
			order = 1,
			defaultValue = "1",
			description = "Enables SNMP protocol version: 1, 2, 3-md5, 3-sha or 3-noauth",
			converter = SnmpVersionConverter.class
	)
	SNMPProtocol.SNMPVersion snmpVersion;

	@Option(
			names = { "--snmp-community", "--community" },
			order = 2,
			defaultValue = "public",
			description = "Community string for SNMP version 1 and 2 (default: ${DEFAULT-VALUE})"
	)
	String community;

	@Option(
			names = "--snmp-username",
			order = 3,
			description = "Username for SNMP version 3 with MD5 or SHA"
	)
	String username;

	@Option(
			names = "--snmp-password",
			order = 4,
			description = "Password for SNMP version 3 with MD5 or SHA",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

	@Option(
			names = "--snmp-privacy",
			order = 5,
			description = "Privacy (encryption type) for SNMP version 3 (DES, AES, or none)",
			converter = SnmpPrivacyConverter.class
	)
	Privacy privacy;

	@Option(
			names = "--snmp-privacy-password",
			order = 6,
			description = "Privacy (encryption) password",
			interactive = true,
			arity = "0..1"
	)
	char[] privacyPassword;

	@Option(
			names = "--snmp-port",
			order = 7,
			defaultValue = "161",
			description = "Port of the SNMP agent (default: ${DEFAULT-VALUE})"
	)
	int port;

	@Option(
			names = "--snmp-timeout",
			order = 8,
			defaultValue = "120",
			description = "Timeout in seconds for SNMP operations (default: ${DEFAULT-VALUE} s)"
	)
	long timeout;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return a SNMPProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public SNMPProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		return SNMPProtocol
				.builder()
				.version(snmpVersion)
				.community(community)
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.privacy(privacy)
				.privacyPassword(privacyPassword)
				.port(port)
				.timeout(timeout)
				.build();
	}
}

