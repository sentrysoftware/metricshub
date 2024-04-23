package org.sentrysoftware.metricshub.extension.oscommand;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub OsCommand Extension
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

import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;

/**
 * Configuration class for SSH-based operations. It includes SSH-specific settings such as username, password,
 * private key, and sudo command configurations. It extends {@link OsCommandConfiguration} by adding more
 * specific fields and validation logic pertinent to SSH configurations.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class SshConfiguration extends OsCommandConfiguration {

	private String username;
	private char[] password;
	private String privateKey;

	@Builder(builderMethodName = "sshConfigurationBuilder")
	public SshConfiguration(
		boolean useSudo,
		Set<String> useSudoCommands,
		String sudoCommand,
		Long timeout,
		String username,
		char[] password,
		String privateKey
	) {
		super(useSudo, useSudoCommands, sudoCommand, timeout);
		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
	}

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			username,
			attr -> attr == null || attr.isBlank(),
			() ->
				String.format(
					"Resource %s - No username configured for protocol %s." +
					" This resource will not be monitored. Please verify the configured username.",
					resourceKey,
					"SSH"
				)
		);

		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid for protocol %s." +
					" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.",
					resourceKey,
					"SSH",
					timeout
				)
		);
	}

	@Override
	public String toString() {
		String desc = "SSH";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
