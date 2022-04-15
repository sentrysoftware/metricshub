package com.sentrysoftware.hardware.agent.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol.WBEMProtocols;

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
public class WbemProtocolDTO extends AbstractProtocolDTO {

	@Default
	WBEMProtocols protocol = WBEMProtocols.HTTPS;

	@Default
	private Integer port = 5989;

	private String namespace;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	String username;

	char[] password;

	/**
	 * Create a new {@link WBEMProtocol} instance based on the current members
	 *
	 * @return The {@link WBEMProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		
		
		return WBEMProtocol
				.builder()
				.namespace(namespace)
				.username(username)
				.password(super.decrypt(password))
				.port(port)
				.protocol(protocol)
				.timeout(timeout)
				.build();
	}

	@Override
	public String toString() {
		String desc = protocol + "/" + port;
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
