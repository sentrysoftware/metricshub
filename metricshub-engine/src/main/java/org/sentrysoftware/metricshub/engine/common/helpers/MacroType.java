package org.sentrysoftware.metricshub.engine.common.helpers;

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

/**
 * Enum representing various types of macros.
 * These constants are used to identify the different pieces of
 * information required for authentication processes.
 */
public enum MacroType {
	USERNAME,
	PASSWORD,
	HOSTNAME,
	AUTHENTICATIONTOKEN,
	PASSWORD_BASE64,
	BASIC_AUTH_BASE64,
	SHA256_AUTH;

	/**
	 * Finds the MacroType corresponding to the provided string key.
	 *
	 * @param key The string representation of the macro type (e.g. "USERNAME", "PASSWORD")
	 * @return The corresponding MacroType, or null if not found.
	 */
	public static MacroType fromString(String key) {
		for (MacroType type : values()) {
			if (type.name().equalsIgnoreCase(key)) {
				return type;
			}
		}
		return null;
	}
}
