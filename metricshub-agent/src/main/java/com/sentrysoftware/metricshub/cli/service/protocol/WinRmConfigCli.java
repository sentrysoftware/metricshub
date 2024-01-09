package com.sentrysoftware.metricshub.cli.service.protocol;

import com.sentrysoftware.metricshub.cli.service.converter.TransportProtocolConverter;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import com.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.sentrysoftware.winrm.service.client.auth.AuthenticationEnum;
import picocli.CommandLine.Option;

@Data
@EqualsAndHashCode(callSuper = true)
public class WinRmConfigCli extends AbstractTransportProtocolCli {

	public static final int DEFAULT_TIMEOUT = 30;
	public static final Integer DEFAULT_HTTP_PORT = 5985;
	public static final Integer DEFAULT_HTTPS_PORT = 5986;

	@Option(names = "--winrm", order = 1, description = "Enables WinRM")
	private boolean useWinRM;

	@Option(
		names = "--winrm-transport",
		order = 2,
		paramLabel = "HTTP|HTTPS",
		defaultValue = "HTTP",
		description = "Transport protocol for WinRM (default: ${DEFAULT-VALUE})",
		converter = TransportProtocolConverter.class
	)
	private TransportProtocols protocol;

	@Option(
		names = { "--winrm-username" },
		order = 3,
		paramLabel = "USER",
		description = "Username for WinRM authentication"
	)
	private String username;

	@Option(
		names = { "--winrm-password" },
		order = 4,
		paramLabel = "P4SSW0RD",
		description = "Password for the WinRM authentication",
		arity = "0..1",
		interactive = true
	)
	private char[] password;

	@Option(
		names = "--winrm-port",
		order = 5,
		paramLabel = "PORT",
		description = "Port for WinRM service (default: 5985 for HTTP, 5986 for HTTPS)"
	)
	private Integer port;

	@Option(
		names = "--winrm-timeout",
		order = 6,
		paramLabel = "TIMEOUT",
		defaultValue = "" + DEFAULT_TIMEOUT,
		description = "Timeout in seconds for WinRM operations (default: ${DEFAULT-VALUE} s)"
	)
	private long timeout;

	@Option(
		names = "--winrm-auth",
		description = "Comma-separated ordered list of authentication schemes." +
		" Possible values are NTLM and KERBEROS. By default, only NTLM is used",
		order = 7,
		paramLabel = "AUTH",
		split = ","
	)
	private List<AuthenticationEnum> authentications;

	@Option(
		names = { "--winrm-force-namespace" },
		order = 8,
		paramLabel = "NAMESPACE",
		description = "Forces a specific namespace for connectors that perform namespace auto-detection (advanced)"
	)
	private String namespace;

	/**
	 * @param defaultUsername Username specified at the top level of the CLI (with the --username option)
	 * @param defaultPassword Password specified at the top level of the CLI (with the --password option)
	 * @return a WinRmProtocol instance corresponding to the options specified by the user in the CLI
	 */
	@Override
	public IConfiguration toProtocol(String defaultUsername, char[] defaultPassword) {
		return WinRmConfiguration
			.builder()
			.username(username == null ? defaultUsername : username)
			.password(username == null ? defaultPassword : password)
			.namespace(namespace)
			.port(getOrDeducePortNumber())
			.protocol(protocol)
			.authentications(authentications)
			.timeout(timeout)
			.build();
	}

	/**
	 * @return Default HTTPS Port Number for WinRM
	 */
	protected int defaultHttpsPortNumber() {
		return 5986;
	}

	/**
	 * @return Default HTTP Port Number for WinRM
	 */
	protected int defaultHttpPortNumber() {
		return 5985;
	}

	/**
	 * Whether HTTPS is configured or not
	 *
	 * @return boolean value
	 */
	@Override
	protected boolean isHttps() {
		return TransportProtocols.HTTPS.equals(protocol);
	}
}
