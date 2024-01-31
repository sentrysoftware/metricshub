package org.sentrysoftware.metricshub.cli.service.converter;

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

import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import picocli.CommandLine;

/**
 * A converter class for converting a given privacy string to {@link SnmpConfiguration.Privacy}.
 */
public class SnmpPrivacyConverter implements CommandLine.ITypeConverter<SnmpConfiguration.Privacy> {

	/**
	 * Converts a given privacy string to {@link SnmpConfiguration.Privacy}
	 *
	 * @param privacy a given privacy string
	 * @return value of type {@link SnmpConfiguration.Privacy}
	 * @throws CommandLine.TypeConversionException if an error occurs during the conversion
	 */
	@Override
	public SnmpConfiguration.Privacy convert(final String privacy) {
		try {
			return SnmpConfiguration.Privacy.interpretValueOf(privacy);
		} catch (Exception e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
