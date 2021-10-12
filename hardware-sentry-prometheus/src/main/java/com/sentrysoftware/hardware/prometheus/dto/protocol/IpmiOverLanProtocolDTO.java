package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeDeserializer;
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
public class IpmiOverLanProtocolDTO implements IProtocolConfigDTO {

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
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
	@Override
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

	@Override
	public String toString() {
		String desc = "IPMI";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
