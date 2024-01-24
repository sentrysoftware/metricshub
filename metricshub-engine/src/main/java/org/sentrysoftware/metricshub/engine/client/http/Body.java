package org.sentrysoftware.metricshub.engine.client.http;

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
import java.util.function.UnaryOperator;

public interface Body extends Serializable {
	/**
	 * Gets the HTTP body content as string and performs macro replacements
	 *
	 * @param username            HTTP username
	 * @param password            HTTP password
	 * @param authenticationToken HTTP authentication token
	 * @param hostname            HTTP server's hostname
	 * @return string value
	 */
	String getContent(String username, char[] password, String authenticationToken, String hostname);

	/**
	 * Performs a deep copy
	 *
	 * @return new {@link Body} instance
	 */
	Body copy();

	/**
	 * Updates the actual body attributes
	 *
	 * @param updater updater function
	 */
	void update(UnaryOperator<String> updater);

	/**
	 * Gets the HTTP body string description
	 *
	 * @return string value
	 */
	String description();
}
