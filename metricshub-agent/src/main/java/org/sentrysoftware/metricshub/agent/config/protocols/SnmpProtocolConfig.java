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
import org.sentrysoftware.metricshub.agent.deserialization.SnmpPrivacyDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.SnmpVersionDeserializer;
import org.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.Privacy;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.SnmpVersion;

/**
 * Configuration class for the SNMP protocol.
 * Extends {@link AbstractProtocolConfig}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SnmpProtocolConfig extends AbstractProtocolConfig {

	@Default
	@JsonDeserialize(using = SnmpVersionDeserializer.class)
	private SnmpVersion version = SnmpVersion.V1;

	@Default
	private char[] community = new char[] { 'p', 'u', 'b', 'l', 'i', 'c' };

	@Default
	private Integer port = 161;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	@JsonDeserialize(using = SnmpPrivacyDeserializer.class)
	private Privacy privacy;

	private char[] privacyPassword;

	private String username;

	private char[] password;

	private String contextName;

	/**
	 * Create a new {@link SnmpConfiguration} instance based on the current members
	 *
	 * @return The {@link SnmpConfiguration} instance
	 */
	@Override
	public IConfiguration toConfiguration() {
		return SnmpConfiguration
			.builder()
			.version(version)
			.community(String.valueOf(decrypt(community)))
			.username(username)
			.password(super.decrypt(password))
			.privacy(privacy)
			.privacyPassword(super.decrypt(privacyPassword))
			.port(port)
			.timeout(timeout)
			.contextName(contextName)
			.build();
	}

	@Override
	public String toString() {
		String desc = version.getDisplayName();
		if (version == SnmpVersion.V1 || version == SnmpVersion.V2C) {
			desc = desc + " (" + String.valueOf(community) + ")";
		} else {
			if (username != null) {
				desc = desc + " as " + username;
			}
			if (privacy != null && privacy != Privacy.NO_ENCRYPTION) {
				desc = desc + " (" + privacy + "-encrypted)";
			}
		}
		return desc;
	}
}
