package org.sentrysoftware.metricshub.cli.service.protocol;

import lombok.Data;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import picocli.CommandLine.Option;

/**
 * This class is used by MetricsHubCliService to configure Wmi protocol when using the MetricsHub CLI.
 * It create the engine's {@link WmiConfiguration} object that is used to monitor a specific resource.
 */
@Data
public class WmiConfigCli implements IProtocolConfigCli {

	/**
	 *  Default timeout in seconds for Wbem operations
	 */
	public static final int DEFAULT_TIMEOUT = 30;

	@Option(names = "--wmi", order = 1, description = "Enables WMI")
	private boolean useWmi;

	/**
	 * Username for WMI authentication
	 */
	@Option(names = "--wmi-username", order = 2, paramLabel = "USER", description = "Username for WMI authentication")
	private String username;

	/**
	 * Password for WMI authentication
	 */
	@Option(
		names = "--wmi-password",
		order = 3,
		paramLabel = "P4SSW0RD",
		description = "Password for WMI authentication",
		interactive = true,
		arity = "0..1"
	)
	private char[] password;

	/**
	 * Timeout in seconds for WBem operations
	 */
	@Option(
		names = "--wmi-timeout",
		order = 4,
		paramLabel = "TIMEOUT",
		defaultValue = "" + DEFAULT_TIMEOUT,
		description = "Timeout in seconds for WBEM operations (default: ${DEFAULT-VALUE} s)"
	)
	private Long timeout;

	/**
	 * Forces a specific namespace for connectors that perform namespace auto-detection
	 */
	@Option(
		names = "--wmi-force-namespace",
		order = 5,
		paramLabel = "NAMESPACE",
		description = "Force a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	/**
	 * This method creates an {@link WmiConfiguration} for a given username and a given password
	 *
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return an WMIProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public IConfiguration toProtocol(String defaultUsername, char[] defaultPassword) {
		return WmiConfiguration
			.builder()
			.username(username == null ? defaultUsername : username)
			.password(username == null ? defaultPassword : password)
			.namespace(namespace)
			.timeout(timeout)
			.build();
	}
}
