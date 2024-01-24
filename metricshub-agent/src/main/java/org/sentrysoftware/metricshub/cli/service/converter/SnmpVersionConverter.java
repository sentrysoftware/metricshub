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

import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import picocli.CommandLine;

/**
 * Custom converter for converting SNMP version strings to {@link SnmpConfiguration.SnmpVersion}.
 * It is used in conjunction with Picocli's command-line parsing to convert command-line input to the appropriate enum type.
 */
public class SnmpVersionConverter implements CommandLine.ITypeConverter<SnmpConfiguration.SnmpVersion> {

	/**
	 * Converts a given version string to {@link SnmpConfiguration.SnmpVersion}
	 *
	 * @param version a given version
	 * @return value of type {@link SnmpConfiguration.SnmpVersion}
	 */
	@Override
	public SnmpConfiguration.SnmpVersion convert(@NonNull final String version) {
		try {
			return SnmpConfiguration.SnmpVersion.interpretValueOf(version);
		} catch (Exception e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
