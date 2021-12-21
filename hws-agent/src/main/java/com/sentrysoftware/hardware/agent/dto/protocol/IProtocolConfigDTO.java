package com.sentrysoftware.hardware.agent.dto.protocol;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;

public interface IProtocolConfigDTO {

	/**
	 * Convert the DTO to the matrix {@link IProtocolConfiguration}
	 * 
	 * @return {@link IProtocolConfiguration} instance
	 */
	IProtocolConfiguration toProtocol();

}
