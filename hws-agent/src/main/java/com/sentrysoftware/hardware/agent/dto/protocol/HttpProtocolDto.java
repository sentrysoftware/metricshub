package com.sentrysoftware.hardware.agent.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
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
public class HttpProtocolDto extends AbstractProtocolDto {

	@Default
	private Boolean https = true;

	@Default
	private Integer port = 443;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	private String username;
	private char[] password;

	/**
	 * Create a new {@link HttpProtocol} instance based on the current members
	 *
	 * @return The {@link HttpProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return HttpProtocol
				.builder()
				.https(https)
				.username(username)
				.password(super.decrypt(password))
				.port(port)
				.timeout(timeout)
				.build();
	}

	@Override
	public String toString() {
		return String.format(
				"%s/%d%s",
				Boolean.TRUE.equals(https) ? "HTTPS" : "HTTP",
				port,
				username != null ? " as " + username : ""
		);
	}
	
	
}
