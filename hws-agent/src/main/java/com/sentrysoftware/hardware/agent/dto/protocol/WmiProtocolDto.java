package com.sentrysoftware.hardware.agent.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;

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
public class WmiProtocolDto extends AbstractProtocolDto {

	private String username;
	private char[] password;
	private String namespace;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WmiProtocol} instance based on the current members
	 *
	 * @return The {@link WmiProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return WmiProtocol
				.builder()
				.namespace(namespace)
				.username(username)
				.password(super.decrypt(password))
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
