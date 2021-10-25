package com.sentrysoftware.hardware.prometheus.dto.protocol;

import org.slf4j.Logger;

import com.sentrysoftware.hardware.prometheus.security.PasswordEncrypt;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.security.SecurityManager;

public interface IProtocolConfigDTO {

	/**
	 * Convert the DTO to the matrix {@link IProtocolConfiguration}
	 * 
	 * @return {@link IProtocolConfiguration} instance
	 */
	IProtocolConfiguration toProtocol();

	/**
	 * Decrypt the given crypted password.
	 * @param crypted
	 * @param logger
	 * @return char array
	 */
	static char[] decrypt(final char[] crypted, final Logger logger) {
		try {
			return SecurityManager.decrypt(crypted, PasswordEncrypt.getKeyStoreFile(false));
		} catch(Exception e) {
			// This is a real problem, let's log the error
			logger.error("Could not decrypt password: {}", e.getMessage());
			logger.debug("Exception", e);
			return crypted;
		}
	}
}
