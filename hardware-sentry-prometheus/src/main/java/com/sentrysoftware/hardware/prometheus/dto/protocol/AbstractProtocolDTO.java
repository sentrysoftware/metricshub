package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.sentrysoftware.hardware.prometheus.security.PasswordEncrypt;
import com.sentrysoftware.matrix.security.SecurityManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractProtocolDTO implements IProtocolConfigDTO {

	/**
	 * Decrypt the given crypted password.
	 * @param crypted
	 * @return char array
	 */
	protected char[] decrypt(final char[] crypted) {
		try {
			return SecurityManager.decrypt(crypted, PasswordEncrypt.getKeyStoreFile(false));
		} catch(Exception e) {
			// This is a real problem, let's log the error
			log.error("Could not decrypt password: {}", e.getMessage());
			log.debug("Exception", e);
			return crypted;
		}
	}

}
