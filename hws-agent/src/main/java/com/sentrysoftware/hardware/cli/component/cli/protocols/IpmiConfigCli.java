package com.sentrysoftware.hardware.cli.component.cli.protocols;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;

import lombok.Data;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Data
public class IpmiConfigCli implements IProtocolConfigCli {

	public static final int DEFAULT_TIMEOUT = 120;

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
			paramLabel = "USER",
			description = "Username for IPMI-over-LAN authentication"
	)
	private String username;

	@Option(
			names = "--ipmi-password",
			order = 3,
			paramLabel = "P4SSW0RD",
			description = "Password for IPMI-over-LAN authentication",
			interactive = true,
			arity = "0..1"
	)
	private char[] password;

	@Option(
			names = "--ipmi-bmc-key",
			order = 4,
			paramLabel = "KEY",
			description = "BMC key for IPMI-over-LAN two-key authentication (in hexadecimal)"
	)
	private String bmcKey;

	@Option(
			names = "--ipmi-skip-auth",
			order = 5,
			defaultValue =  "false",
			description = "Whether to skip IPMI-over-LAN authentication"
	)
	private boolean skipAuth;

	@Option(
			names = "--ipmi-timeout",
			order = 6,
			paramLabel = "TIMEOUT",
			defaultValue = "" + DEFAULT_TIMEOUT,
			description = "Timeout in seconds for HTTP operations (default: ${DEFAULT-VALUE} s)"
	)
	private long timeout;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an IPMIOverLanProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
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
