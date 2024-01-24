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

import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * Converter for {@link TransportProtocols} used in command-line argument parsing.
 * Implements {@link ITypeConverter}.
 */
public class TransportProtocolConverter implements ITypeConverter<TransportProtocols> {

	/**
	 * Converts the input String to a {@link TransportProtocols} enum value.
	 *
	 * @param transportProtocol The input String representing a transport protocol.
	 * @return The corresponding {@link TransportProtocols} enum value.
	 * @throws TypeConversionException If the conversion fails, typically due to an invalid input.
	 */
	@Override
	public TransportProtocols convert(final String transportProtocol) throws Exception {
		try {
			return TransportProtocols.interpretValueOf(transportProtocol);
		} catch (Exception e) {
			throw new TypeConversionException(e.getMessage());
		}
	}
}
