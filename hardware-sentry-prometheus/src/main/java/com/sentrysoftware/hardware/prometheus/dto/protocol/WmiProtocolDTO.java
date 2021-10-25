package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class WmiProtocolDTO implements IProtocolConfigDTO {

	private String username;
	private char[] password;
	private String namespace;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WMIProtocol} instance based on the current members
	 *
	 * @return The {@link WMIProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return WMIProtocol
				.builder()
				.namespace(namespace)
				.username(username)
				.password(IProtocolConfigDTO.decrypt(password, log))
				.timeout(timeout)
				.build();
	}

	@Override
	public String toString() {
		String desc = "WMI";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
