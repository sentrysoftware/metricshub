package com.sentrysoftware.metricshub.cli.service.protocol;

import com.sentrysoftware.metricshub.engine.common.helpers.ArrayHelper;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;
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

	@Option(names = "--ipmi", order = 1, description = "Enables IPMI-over-LAN")
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
		defaultValue = "false",
		description = "Skips IPMI-over-LAN authentication"
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
	 * This method creates an {@link IpmiConfiguration} for a given username and a given password
	 *
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an {@link IpmiConfiguration} instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public IConfiguration toProtocol(final String defaultUsername, final char[] defaultPassword) {
		try {
			return IpmiConfiguration
				.builder()
				.username(username == null ? defaultUsername : username)
				.password(username == null ? defaultPassword : password)
				.bmcKey(ArrayHelper.hexToByteArray(bmcKey))
				.skipAuth(skipAuth)
				.timeout(timeout)
				.build();
		} catch (Exception e) {
			throw new ParameterException(spec.commandLine(), e.getMessage());
		}
	}
}
