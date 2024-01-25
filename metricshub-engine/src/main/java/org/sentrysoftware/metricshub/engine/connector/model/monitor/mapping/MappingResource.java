package org.sentrysoftware.metricshub.engine.connector.model.monitor.mapping;

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a mapping resource used in monitor configuration.
 *
 * <p>
 * A {@code MappingResource} instance holds information about the type and attributes of a mapping resource.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingResource implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The type of the mapping resource.
	 */
	private String type;

	/**
	 * The attributes associated with the mapping resource.
	 */
	@Builder.Default
	private Map<String, String> attributes = new HashMap<>();

	/**
	 * Whether the type is defined or not
	 *
	 * @return boolean value
	 */
	public boolean hasType() {
		return type != null && !type.isBlank();
	}
}
