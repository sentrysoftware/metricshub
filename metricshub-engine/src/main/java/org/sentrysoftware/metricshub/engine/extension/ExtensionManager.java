package org.sentrysoftware.metricshub.engine.extension;

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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Manages and aggregates various types of extensions used within MetricsHub.
 * This class acts as a central point for accessing and managing protocol
 * extensions, strategy provider extensions, and connector store provider
 * extensions.
 * <p>
 * The {@link ExtensionManager} is designed to be flexible and extensible,
 * supporting various types of extensions that can be added or removed as
 * needed. It uses the Builder pattern to simplify instantiation and setup.
 * </p>
 */
@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class ExtensionManager {

	@Default
	private List<IProtocolExtension> protocolExtensions = new ArrayList<>();

	@Default
	private List<IStrategyProviderExtension> strategyProviderExtensions = new ArrayList<>();

	@Default
	private List<IConnectorStoreProviderExtension> connectorStoreProviderExtensions = new ArrayList<>();

	/**
	 * Create a new empty instance of the Extension Manager.
	 * @return a new instance of {@link ExtensionManager}.
	 */
	public static ExtensionManager empty() {
		return ExtensionManager.builder().build();
	}

	/**
	 * Find the extension which satisfies the processing of the given criterion according to the user's configuration.
	 *
	 * @param criterion        Any {@link Criterion} implementation
	 * @param telemetryManager {@link TelemetryManager} instance where the configurations are located.
	 * @return an {@link Optional} of an {@link IProtocolExtension} instance.
	 */
	public Optional<IProtocolExtension> findCriterionExtension(
		final Criterion criterion,
		final TelemetryManager telemetryManager
	) {
		return protocolExtensions
			.stream()
			.filter(extension ->
				telemetryManager
					.getHostConfiguration()
					.getConfigurations()
					.values()
					.stream()
					.anyMatch(extension::isValidConfiguration)
			)
			.filter(extension -> extension.getSupportedCriteria().contains(criterion.getClass()))
			.findFirst();
	}

	/**
	 * Find the extension which satisfies the processing of the given source according to the user's configuration.
	 *
	 * @param source           Any {@link Source} implementation
	 * @param telemetryManager {@link TelemetryManager} instance where the configurations are located.
	 * @return an {@link Optional} of an {@link IProtocolExtension} instance.
	 */
	public Optional<IProtocolExtension> findSourceExtension(
		final Source source,
		final TelemetryManager telemetryManager
	) {
		return protocolExtensions
			.stream()
			.filter(extension ->
				telemetryManager
					.getHostConfiguration()
					.getConfigurations()
					.values()
					.stream()
					.anyMatch(extension::isValidConfiguration)
			)
			.filter(extension -> extension.getSupportedSources().contains(source.getClass()))
			.findFirst();
	}

	/**
	 * Find the extensions that satisfy the protocol check according to the user's configuration.
	 *
	 * @param telemetryManager {@link TelemetryManager} instance where the configurations are located.
	 * @return a {@link List} of {@link IProtocolExtension} instances.
	 */
	public List<IProtocolExtension> findProtocolCheckExtensions(@NonNull TelemetryManager telemetryManager) {
		return protocolExtensions
			.stream()
			.filter(extension ->
				telemetryManager
					.getHostConfiguration()
					.getConfigurations()
					.values()
					.stream()
					.anyMatch(extension::isValidConfiguration)
			)
			.collect(Collectors.toList());
	}

	/**
	 * Find a mapping between configuration classes and their corresponding sets
	 * of source classes.
	 * @return A map where the keys are classes extending {@link IConfiguration}
	 *         representing different types of protocol configurations, and the
	 *         values are sets of classes extending {@link Source}, indicating the
	 *         sources that are compatible and can be utilized with each
	 *         configuration type for data exchange operations.
	 */
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> findConfigurationToSourceMapping() {
		return protocolExtensions
			.stream()
			.map(IProtocolExtension::getConfigurationToSourceMapping)
			.flatMap(map -> map.entrySet().stream())
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue));
	}

	/**
	 * Constructs a configuration object from a given JSON node based on the specified configuration type.
	 * This method iterates over a collection of protocol extensions to find the first extension that supports
	 * the specified configuration type. It then uses this extension to build a configuration object from the
	 * provided JSON node. If no suitable extension is found, an <code>null</code> value is returned.
	 *
	 * @param configurationType     A {@link String} representing the type of configuration to be created.
	 *                              This is used to identify the appropriate protocol extension that can
	 *                              handle the specified configuration type.
	 * @param configurationJsonNode A {@link JsonNode} containing the configuration data in JSON format.
	 *                              This data is used by the selected protocol extension to build the configuration object.
	 * @param decrypt               A {@link UnaryOperator} function that takes a {@code char[]} array and
	 *                              returns a {@code char[]} array. This function is intended to decrypt the configuration
	 *                              data if necessary before building the configuration object.
	 * @return An {@link Optional} of {@link IConfiguration} representing the constructed configuration object.
	 *         Returns an empty {@link Optional} if no suitable protocol extension is found or if the configuration
	 *         object cannot be created for any reason.
	 * @throws InvalidConfigurationException If the given configuration JSON node is invalid.
	 */
	public Optional<IConfiguration> buildConfigurationFromJsonNode(
		final String configurationType,
		final JsonNode configurationJsonNode,
		final UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		for (IProtocolExtension extension : protocolExtensions) {
			if (extension.isSupportedConfigurationType(configurationType)) {
				return Optional.ofNullable(extension.buildConfiguration(configurationType, configurationJsonNode, decrypt));
			}
		}

		return Optional.empty();
	}
}