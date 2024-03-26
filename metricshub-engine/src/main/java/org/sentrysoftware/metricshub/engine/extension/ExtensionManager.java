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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;
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
}
