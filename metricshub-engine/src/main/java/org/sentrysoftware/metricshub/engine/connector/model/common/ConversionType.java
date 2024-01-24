package org.sentrysoftware.metricshub.engine.connector.model.common;

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

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum ConversionType {
	@JsonAlias(value = { "hex2Dec", "hex_2_dec" })
	HEX_2_DEC("hex2Dec"),
	@JsonAlias(value = { "array2SimpleStatus", "array_2_simple_status" })
	ARRAY_2_SIMPLE_STATUS("array2SimpleStatus");

	@Getter
	private String name;

	/**
	 * Get {@link ConversionType} by name, the name defined in the connector code
	 *
	 * @param name The name in the connector defining a {@link ConversionType}. E.g. hex2dec
	 * @return {@link ConversionType} instance
	 */
	public static ConversionType getByName(@NonNull final String name) {
		return Arrays
			.stream(ConversionType.values())
			.filter(n -> name.equalsIgnoreCase(n.getName()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Undefined conversion type: " + name));
	}
}
