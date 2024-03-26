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
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

/**
 * Custom JSON deserializer for deserializing a JSON object into an {@link IConfiguration} implementation
 * using the {@link ExtensionManager}.
 */
public class ExtensionConfigDeserializer extends JsonDeserializer<IConfiguration> {

	@Override
	public IConfiguration deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		final ExtensionManager extensionManager = (ExtensionManager) ctxt.findInjectableValue(
			ExtensionManager.class.getName(),
			null,
			null
		);

		final JsonNode jsonNode = parser.readValueAs(JsonNode.class);

		final String configurationType = parser.getCurrentName();

		return extensionManager
			.buildConfigurationFromJsonNode(configurationType, jsonNode, ConfigHelper::decrypt)
			.orElseThrow(() ->
				new IOException("Cannot read " + configurationType + " credentials. Check extensions presence.")
			);
	}
}
