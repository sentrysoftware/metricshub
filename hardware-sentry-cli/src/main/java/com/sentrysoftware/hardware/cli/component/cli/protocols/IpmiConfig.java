package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;

import lombok.Data;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Data
public class IpmiConfig {

	@Spec
	CommandSpec spec;

	@Option(
			names = "--ipmi",
			order = 1,
			description = "Enables IPMI-over-LAN"
	)
	private boolean useIpmi;

	@Option(
			names = "--ipmi-username",
			order = 2,
			description = "Username for IPMI-over-LAN authentication"
	)
	String username;

	@Option(
			names = "--ipmi-password",
			order = 3,
			description = "Password for IPMI-over-LAN authentication",
			interactive = true,
			arity = "0..1"
	)
	char[] password;

	@Option(
			names = "--ipmi-bmc-key",
			order = 4,
			description = "BMC key for IPMI-over-LAN two-key authentication (in hexadecimal)"
	)
	String bmcKey;

	@Option(
			names = "--ipmi-skip-auth",
			order = 5,
			defaultValue =  "false",
			description = "Whether to skip IPMI-over-LAN authentication"
	)
	boolean skipAuth;

	@Option(
			names = "--ipmi-timeout",
			order = 6,
			defaultValue = "120",
			description = "IPMI-over-LAN timeout, in seconds (default: 120)"
	)
	long timeout;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an IPMIOverLanProtocol instance corresponding to the options specified by the user in the CLI
	 */
	public IPMIOverLanProtocol toProtocol(String defaultUsername, char[] defaultPassword) {
		try {
			return IPMIOverLanProtocol
					.builder()
					.username(username == null ? defaultUsername : username)
					.password(username == null ? defaultPassword : password)
					.bmcKey(ArrayHelper.hexToByteArray(bmcKey))
					.skipAuth(skipAuth)
					.timeout(timeout)
					.build();
		} catch (IllegalArgumentException e) {
			throw new ParameterException(spec.commandLine(), e.getMessage());
		}
	}

}
