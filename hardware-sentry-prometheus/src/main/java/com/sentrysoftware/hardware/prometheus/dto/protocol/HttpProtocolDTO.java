package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class HttpProtocolDTO implements IProtocolConfigDTO {

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
	 * Create a new {@link HTTPProtocol} instance based on the current members
	 *
	 * @return The {@link HTTPProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return HTTPProtocol
				.builder()
				.https(https)
				.username(username)
				.password(IProtocolConfigDTO.decrypt(password, log))
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
