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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

/**
 * The WmiConfiguration interface represents the configuration for the Windows Management Instrumentation protocol in the MetricsHub engine.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WmiConfiguration implements IWinConfiguration {

	private String username;
	private char[] password;
	private String namespace;

	@Builder.Default
	private Long timeout = 120L;

	@Override
	public String toString() {
		String description = "WMI";
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		// TODO implement the validation
	}
}
