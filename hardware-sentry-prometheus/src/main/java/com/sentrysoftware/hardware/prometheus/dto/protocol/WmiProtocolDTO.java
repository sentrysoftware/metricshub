package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeoutDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WmiProtocolDTO {

	private String username;
	private char[] password;
	private String namespace;

	@Default
	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WMIProtocol} instance based on the current members
	 *
	 * @return The {@link WMIProtocol} instance
	 */
	public IProtocolConfiguration toProtocol() {
		return WMIProtocol
				.builder()
				.namespace(namespace)
				.username(username)
				.password(password)
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
