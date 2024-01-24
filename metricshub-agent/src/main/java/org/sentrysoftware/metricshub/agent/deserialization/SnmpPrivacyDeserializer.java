package org.sentrysoftware.metricshub.agent.deserialization;

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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import org.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration.Privacy;

/**
 * Custom deserializer for converting SNMP privacy string representations to {@link Privacy}.
 * It is used in conjunction with Jackson's JSON deserialization to convert JSON values to the appropriate enum type.
 */
public class SnmpPrivacyDeserializer extends JsonDeserializer<Privacy> {

	/**
	 * Deserializes a JSON value (in the form of a JsonParser) into the corresponding {@link Privacy} enum.
	 *
	 * @param parser The JsonParser containing the JSON value to be deserialized
	 * @param ctxt   The DeserializationContext
	 * @return The deserialized {@link Privacy} enum value
	 * @throws IOException If an I/O error occurs during deserialization
	 */
	@Override
	public Privacy deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return Privacy.interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}
}
