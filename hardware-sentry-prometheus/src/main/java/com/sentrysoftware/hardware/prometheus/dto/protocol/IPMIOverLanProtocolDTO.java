package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeoutDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IPMIOverLanProtocolDTO {

	@Default
	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout = 120L;

	private String username;
	private char[] password;
	private byte[] bmcKey;
	private boolean skipAuth;

	/**
	 * Create a new {@link IPMIOverLanProtocol} instance based on the current members
	 *
	 * @return The {@link IPMIOverLanProtocol} instance
	 */
	public IProtocolConfiguration toProtocol() {
		return IPMIOverLanProtocol
				.builder()
				.username(username)
				.password(password)
				.bmcKey(bmcKey)
				.skipAuth(skipAuth)
				.timeout(timeout)
				.build();
	}
}
