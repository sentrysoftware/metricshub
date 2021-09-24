package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeoutDeserializer;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
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
public class HttpProtocolDTO {

	@Default
	private Boolean https = true;

	@Default
	private Integer port = 443;

	@Default
	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout = 120L;

	private String username;
	private char[] password;

	/**
	 * Create a new {@link HTTPProtocol} instance based on the current members
	 *
	 * @return The {@link HTTPProtocol} instance
	 */
	public IProtocolConfiguration toProtocol() {
		return HTTPProtocol
				.builder()
				.https(https)
				.username(username)
				.password(password)
				.port(port)
				.timeout(timeout)
				.build();
	}

	@Override
	public String toString() {
		return String.format(
				"%s/%d%s",
				https ? "HTTPS" : "HTTP",
				port,
				username != null ? " as " + username : ""
		);
	}
}
