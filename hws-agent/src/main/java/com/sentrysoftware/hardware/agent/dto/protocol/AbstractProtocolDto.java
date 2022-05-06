package com.sentrysoftware.hardware.agent.dto.protocol;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;

public abstract class AbstractProtocolDto implements IProtocolConfigDto {

	/**
	 * Decrypt the given crypted password.
	 * 
	 * @param crypted
	 * @return char array
	 */
	protected char[] decrypt(final char[] crypted) {
		return ConfigHelper.decrypt(crypted);
	}

}
