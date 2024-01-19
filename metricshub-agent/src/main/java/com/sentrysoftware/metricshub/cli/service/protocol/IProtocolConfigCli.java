package com.sentrysoftware.metricshub.cli.service.protocol;

import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;

/**
 * Interface for CLI configurations of protocols to convert to core engine configurations.
 */
public interface IProtocolConfigCli {
	/**
	 * Convert the CLI configuration to the core engine configuration
	 *
	 * @param defaultUsername Username to use in case the protocol username is undefined
	 * @param defaultPassword Password to use in case the protocol password is undefined
	 *
	 * @return Instance of {@link IConfiguration}
	 */
	IConfiguration toProtocol(String defaultUsername, char[] defaultPassword);
}
