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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

/**
 * Deserializer for extension protocols. Converts JSON into a Map of protocol
 * names to IConfiguration instances.
 */
@Slf4j
public class ExtensionProtocolsDeserializer extends JsonDeserializer<Map<String, IConfiguration>> {

	/**
	 * Deserializes JSON content into a Map of protocol configurations.
	 *
	 * @param parser the JSON parser
	 * @param ctxt   the deserialization context
	 * @return a Map of protocol names to IConfiguration instances
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public Map<String, IConfiguration> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		JsonNode node = parser.getCodec().readTree(parser);
		Map<String, IConfiguration> protocols = new HashMap<>();

		if (node != null) {
			// Retrieve the ExtensionManager from the context
			final ExtensionManager extensionManager = (ExtensionManager) ctxt.findInjectableValue(
				ExtensionManager.class.getName(),
				null,
				null
			);

			node
				.fields()
				.forEachRemaining(entry -> {
					final String protocolName = entry.getKey();
					final JsonNode protocolConfigNode = entry.getValue();

					final Optional<IConfiguration> protocolConfig = buildConfigurationFromJsonNode(
						extensionManager,
						protocolName,
						protocolConfigNode
					);

					protocolConfig.ifPresent(config -> protocols.put(protocolName, config));
				});
		}

		return protocols;
	}

	/**
	 * Builds a protocol configuration from the given JSON node.
	 *
	 * @param extensionManager the ExtensionManager instance
	 * @param protocolName     the name of the protocol
	 * @param configNode       the JSON node containing the protocol configuration
	 * @return an IConfiguration instance or null if an error occurs
	 */
	private Optional<IConfiguration> buildConfigurationFromJsonNode(
		ExtensionManager extensionManager,
		String protocolName,
		JsonNode configNode
	) {
		if (protocolName.isBlank() || configNode == null) {
			log.error("Invalid protocol name or configuration node.");
			return Optional.empty();
		}

		try {
			return extensionManager.buildConfigurationFromJsonNode(protocolName, configNode, ConfigHelper::decrypt);
		} catch (Exception e) {
			log.error("Failed to build protocol configuration for {}: {}", protocolName, e.getMessage());
			log.debug("Failed to build protocol configuration for {}", protocolName, e);
			return Optional.empty();
		}
	}
}
