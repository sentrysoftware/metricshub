package com.sentrysoftware.hardware.agent.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class IpmiOverLanProtocolDto extends AbstractProtocolDto {

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	private String username;
	private char[] password;
	private byte[] bmcKey;
	private boolean skipAuth;

	/**
	 * Create a new {@link IpmiOverLanProtocol} instance based on the current members
	 *
	 * @return The {@link IpmiOverLanProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return IpmiOverLanProtocol
				.builder()
				.username(username)
				.password(super.decrypt(password))
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
