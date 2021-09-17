package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeoutDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol.WBEMProtocols;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WBEMProtocolDTO {

	@Default
	WBEMProtocols protocol = WBEMProtocols.HTTPS;

	@Default
	private Integer port = 5989;

	private String namespace;

	@Default
	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout = 120L;

	String username;

	char[] password;

	/**
	 * Create a new {@link WBEMProtocol} instance based on the current members
	 *
	 * @return The {@link WBEMProtocol} instance
	 */
	public IProtocolConfiguration toProtocol() {
		return WBEMProtocol
				.builder()
				.namespace(namespace)
				.username(username)
				.password(password)
				.port(port)
				.protocol(protocol)
				.timeout(timeout)
				.build();
	}
}
