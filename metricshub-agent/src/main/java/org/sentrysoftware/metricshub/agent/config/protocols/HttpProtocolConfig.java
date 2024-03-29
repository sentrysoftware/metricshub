package org.sentrysoftware.metricshub.agent.config.protocols;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.configuration.HttpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.deserialization.TimeDeserializer;

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
