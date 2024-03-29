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
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

/**
 * Custom JSON deserializer for deserializing a JSON object into an {@link IConfiguration} implementation
 * using the {@link ExtensionManager}.
 */
@Slf4j
public class ExtensionConfigDeserializer extends JsonDeserializer<IConfiguration> {

	@Override
	public IConfiguration deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		// Retrieve the ExtensionManager which has been injected in the ObjectMapper currently managed in the AgentContext.
		final ExtensionManager extensionManager = (ExtensionManager) ctxt.findInjectableValue(
			ExtensionManager.class.getName(),
			null,
			null
		);

		final JsonNode jsonNode = parser.readValueAs(JsonNode.class);

		final String configurationType = parser.getCurrentName();

		try {
			return extensionManager
				.buildConfigurationFromJsonNode(configurationType, jsonNode, ConfigHelper::decrypt)
				.orElse(null);
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Cannot read %1$s credentials. Check %1$s configuration format.",
				configurationType
			);
			log.error(errorMessage);
			log.debug(errorMessage, e);
			throw new IOException(errorMessage, e);
		}
	}
}
