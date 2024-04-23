package org.sentrysoftware.metricshub.agent.extension;

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

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;

@Data
@NoArgsConstructor
public class OsCommandTestConfiguration implements IConfiguration {

	public static final Long DEFAULT_TIMEOUT = 30L;
	boolean useSudo;
	Set<String> useSudoCommands = new HashSet<>();
	String sudoCommand = "sudo";
	Long timeout = DEFAULT_TIMEOUT;

	/**
	 * Creates a new instance of OsCommandConfiguration using the provided parameters.
	 *
	 * @param useSudo          Indicates whether to use sudo for executing commands.
	 * @param useSudoCommands  The set of commands for which sudo will be used.
	 * @param sudoCommand      The sudo command to use.
	 * @param timeout          The timeout for executing commands.
	 */
	@Builder
	public OsCommandTestConfiguration(
		final boolean useSudo,
		final Set<String> useSudoCommands,
		final String sudoCommand,
		final Long timeout
	) {
		this.useSudo = useSudo;
		this.useSudoCommands = useSudoCommands == null ? new HashSet<>() : useSudoCommands;
		this.sudoCommand = sudoCommand == null ? "sudo" : sudoCommand;
		this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
	}

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid for protocol %s." +
					" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.",
					resourceKey,
					"OSCommand",
					timeout
				)
		);
	}

	@Override
	public String toString() {
		return "Local Commands";
	}
}
