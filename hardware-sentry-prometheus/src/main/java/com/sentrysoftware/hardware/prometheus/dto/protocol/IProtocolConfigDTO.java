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
	 * @return char array
	 */
	static char[] decrypt(char[] crypted, Logger logger) {
		try {
			return SecurityManager.decrypt(crypted, PasswordEncrypt.getKeyStoreFile(false));
		} catch(Exception e) {
			logger.warn("Could not decrypt password: {}", e.getMessage());
			return crypted;
		}
	}
}
