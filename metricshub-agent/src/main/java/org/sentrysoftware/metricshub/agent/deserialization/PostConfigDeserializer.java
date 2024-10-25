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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.COMMA;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOST_NAME;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.DelegatingDeserializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.sentrysoftware.metricshub.agent.config.AgentConfig;
import org.sentrysoftware.metricshub.agent.config.ResourceConfig;
import org.sentrysoftware.metricshub.agent.config.ResourceGroupConfig;

/**
 * Custom JSON deserializer for post-processing the deserialization of {@link ResourceGroupConfig} instances.
 *
 * <p>The {@code PostConfigDeserializer} extends {@link DelegatingDeserializer} to allow additional processing
 * after the default deserialization. It specifically handles cases where a {@code ResourceGroupConfig} contains
 * {@code ResourceConfig} instances with a special attribute "host.names," indicating multiple hosts sharing
 * the same configuration. This deserializer resolves such configurations by creating new {@code ResourceConfig}
 * instances for each host and updating the configuration map accordingly.
 */
public class PostConfigDeserializer extends DelegatingDeserializer {

	private static final String MULTI_HOST_ATTRIBUTE_KEY = "host.names";

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code PostConfigDeserializer} with the specified delegate.
	 *
	 * @param delegate The delegate {@link JsonDeserializer} for actual deserialization.
	 */
	public PostConfigDeserializer(final JsonDeserializer<?> delegate) {
		super(delegate);
	}

	@Override
	protected JsonDeserializer<?> newDelegatingInstance(JsonDeserializer<?> newDelegatee) {
		return new PostConfigDeserializer(newDelegatee);
	}

	@Override
	public Object deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
		final Object deserializedObject = super.deserialize(jsonParser, ctxt);

		// Manage ResourceConfig instances that define multiple hosts sharing the same configuration
		if (deserializedObject instanceof ResourceGroupConfig resourceGroupConfig) {
			resolveMultiHostResourceConfigurations(resourceGroupConfig);
		}

		// Manage ResourceConfig instances that define multiple hosts sharing the same configuration under the AgentConfig
		if (deserializedObject instanceof AgentConfig agentConfig) {
			resolveResources(agentConfig.getResources());
		}

		return deserializedObject;
	}

	/**
	 * Resolves {@link ResourceConfig} defining multiple hosts that share the same configuration
	 *
	 * @param resourceGroupConfig {@link ResourceGroupConfig} instance where all
	 *                            the {@link ResourceConfig} instances belong
	 */
	private void resolveMultiHostResourceConfigurations(final ResourceGroupConfig resourceGroupConfig) {
		resolveResources(resourceGroupConfig.getResources());
	}

	/**
	 * Resolves the multiple host configurations of the given {@link ResourceConfig} map.
	 * <br>It creates new {@link ResourceConfig} instances for each host and updates the map accordingly.
	 * @param existingResourceConfigMap Map containing the existing {@link ResourceConfig} instances
	 */
	private void resolveResources(final Map<String, ResourceConfig> existingResourceConfigMap) {
		final Set<String> resolvedMultiResourceConfigKeys = new HashSet<>();
		final Map<String, ResourceConfig> newResourceConfigMap = new HashMap<>();
		// Loop over each multiple host configuration and resolve it
		existingResourceConfigMap
			.entrySet()
			.stream()
			.filter(resourceConfigEntry ->
				Objects.nonNull(resourceConfigEntry.getValue()) &&
				resourceConfigEntry.getValue().getAttributes().containsKey(MULTI_HOST_ATTRIBUTE_KEY)
			)
			.forEach(resourceConfigEntry ->
				resolveMultiHostResourceConfig(resolvedMultiResourceConfigKeys, newResourceConfigMap, resourceConfigEntry)
			);

		// Put the new resource configuration (one per host)
		existingResourceConfigMap.putAll(newResourceConfigMap);

		// Removed resolved resouce configurations that define multiple host
		resolvedMultiResourceConfigKeys.forEach(existingResourceConfigMap::remove);
	}

	/**
	 * Resolve the given <code>resourceConfigEntry</code>, generate new resource configurations and update the new resource configuration map.
	 * <br>Each resolved resource configuration key is saved for removal.
	 *
	 * @param resolvedMultiResourceKeys Set of keys that are resolved
	 * @param newResourceConfigMap      Map containing the new {@link ResourceConfig} instances
	 * @param multiResourceConfigEntry  {@link ResourceConfig} entry containing the multiple hosts resource configuration
	 */
	private void resolveMultiHostResourceConfig(
		final Set<String> resolvedMultiResourceKeys,
		final Map<String, ResourceConfig> newResourceConfigMap,
		final Entry<String, ResourceConfig> multiResourceConfigEntry
	) {
		final String multiResourceConfigKey = multiResourceConfigEntry.getKey();
		// Save the key for removal
		resolvedMultiResourceKeys.add(multiResourceConfigKey);

		// Get the multiple ResourceConfig instance
		final ResourceConfig multiResourceConfig = multiResourceConfigEntry.getValue();

		// Get host.names values which is already prepared as CSV
		final String hostnamesCsv = multiResourceConfig.getAttributes().get(MULTI_HOST_ATTRIBUTE_KEY);

		// Loop over each host name value and create a new ResourceConfig using the same configuration
		for (final String hostnameValue : hostnamesCsv.split(COMMA)) {
			// Shallow copy the resource configuration except its attributes map
			final ResourceConfig newResourceConfig = multiResourceConfig.copy();

			final Map<String, String> newAttributes = newResourceConfig.getAttributes();

			// host.names is no more required
			newAttributes.remove(MULTI_HOST_ATTRIBUTE_KEY);
			final String hostname = hostnameValue.trim();

			// Add the host.name attribute
			newAttributes.put(HOST_NAME, hostname);

			// Build a new unique identifier and save the new resource configuration
			newResourceConfigMap.put(String.format("%s-%s", multiResourceConfigKey, hostname), newResourceConfig);
		}
	}
}
