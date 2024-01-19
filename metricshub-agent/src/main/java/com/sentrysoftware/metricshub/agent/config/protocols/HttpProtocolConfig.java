package com.sentrysoftware.metricshub.agent.config.protocols;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Configuration class for the HTTP protocol.
 * Extends {@link AbstractProtocolConfig}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HttpProtocolConfig extends AbstractProtocolConfig {

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
	 * Create a new {@link HttpConfiguration} instance based on the current members
	 *
	 * @return The {@link HttpConfiguration} instance
	 */
	@Override
	public IConfiguration toConfiguration() {
		return HttpConfiguration
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
