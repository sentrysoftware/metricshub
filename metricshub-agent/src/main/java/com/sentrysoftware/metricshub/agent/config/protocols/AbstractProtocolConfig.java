package com.sentrysoftware.metricshub.agent.config.protocols;

import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;

/**
 * This abstract class defines a generic protocol configuration that will be extended to configure all the protocols such as {@link SshProtocolConfig} or {@link IpmiProtocolConfig}).
 */
public abstract class AbstractProtocolConfig {

	/**
	 * Decrypt the given encrypted password.
	 *
	 * @param encrypted The encrypted password
	 * @return char array that contain the decrypted password
	 */
	protected char[] decrypt(final char[] encrypted) {
		return ConfigHelper.decrypt(encrypted);
	}

	/**
	 * Convert the configuration to the engine {@link IConfiguration}
	 *
	 * @return {@link IConfiguration} instance
	 */
	public abstract IConfiguration toConfiguration();
}
