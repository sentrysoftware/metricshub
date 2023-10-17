package com.sentrysoftware.metricshub.agent.config.protocols;

import com.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;

public abstract class AbstractProtocolConfig {

	/**
	 * Decrypt the given encrypted password.
	 *
	 * @param encrypted
	 * @return char array
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
