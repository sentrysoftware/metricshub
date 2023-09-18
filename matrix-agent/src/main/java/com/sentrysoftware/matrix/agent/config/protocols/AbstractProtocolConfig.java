package com.sentrysoftware.matrix.agent.config.protocols;

import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.configuration.IConfiguration;

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
	 * Convert the configuration to the matrix {@link IConfiguration}
	 *
	 * @return {@link IConfiguration} instance
	 */
	abstract IConfiguration toProtocol();
}
