package org.sentrysoftware.metricshub.engine.configuration;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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

@Data
@NoArgsConstructor
public class OsCommandConfiguration implements IConfiguration {

	private static final String SUDO = "sudo";
	public static final Long DEFAULT_TIMEOUT = 30L;
	private boolean useSudo;
	private Set<String> useSudoCommands = new HashSet<>();
	private String sudoCommand = SUDO;
	private Long timeout = DEFAULT_TIMEOUT;

	@Builder
	public OsCommandConfiguration(
		final boolean useSudo,
		final Set<String> useSudoCommands,
		final String sudoCommand,
		final Long timeout
	) {
		this.useSudo = useSudo;
		this.useSudoCommands = useSudoCommands == null ? new HashSet<>() : useSudoCommands;
		this.sudoCommand = sudoCommand == null ? SUDO : sudoCommand;
		this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
	}

	@Override
	public String toString() {
		return "Local Commands";
	}
}
