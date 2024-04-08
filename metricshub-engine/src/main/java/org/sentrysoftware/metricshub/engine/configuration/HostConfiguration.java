package org.sentrysoftware.metricshub.engine.configuration;

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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_JOB_TIMEOUT;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.alert.AlertInfo;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;

/**
 * The HostConfiguration class represents the configuration for a host in the MetricsHub engine.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostConfiguration {

	private String hostname;
	private String hostId;
	private DeviceKind hostType;

	@Default
	private long strategyTimeout = DEFAULT_JOB_TIMEOUT;

	private Set<String> connectors;
	private boolean sequential;
	private Consumer<AlertInfo> alertTrigger;
	private long retryDelay;
	private Map<String, String> connectorVariables;

	@Default
	private Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();

	private String configuredConnectorId;

	private static final Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> CONFIGURATION_TO_SOURCES_MAP;

	static {
		CONFIGURATION_TO_SOURCES_MAP =
			Map.of(
				WmiConfiguration.class,
				Collections.singleton(WmiSource.class),
				WbemConfiguration.class,
				Collections.singleton(WbemSource.class),
				SshConfiguration.class,
				Collections.singleton(OsCommandSource.class),
				IpmiConfiguration.class,
				Collections.singleton(IpmiSource.class),
				OsCommandConfiguration.class,
				Collections.singleton(OsCommandSource.class),
				WinRmConfiguration.class,
				Collections.singleton(WmiSource.class)
			);
	}

	/**
	 * Determine the accepted sources that can be executed using the current engine configuration
	 *
	 * @param isLocalhost      Whether the host should be localhost or not.
	 * @param extensionManager Where all the extensions are managed.
	 * @return {@link Set} of accepted source types
	 */
	public Set<Class<? extends Source>> determineAcceptedSources(
		final boolean isLocalhost,
		final ExtensionManager extensionManager
	) {
		// Retrieve the configuration to Source mapping through the available extensions
		// @formatter:off
		final Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> configurationToSourceMappingFromExtensions =
			extensionManager.findConfigurationToSourceMapping();
		// @formatter:on

		final Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> configurationToSourceMapping =
			new HashMap<>();

		configurationToSourceMapping.putAll(configurationToSourceMappingFromExtensions);
		// TODO Remove this merge when all the extensions are developed
		configurationToSourceMapping.putAll(CONFIGURATION_TO_SOURCES_MAP);

		// protocolConfigurations and host cannot never be null
		final Set<Class<? extends IConfiguration>> protocolTypes = configurations.keySet();

		final Set<Class<? extends Source>> sources = configurationToSourceMapping
			.entrySet()
			.stream()
			.filter(protocolEntry -> protocolTypes.contains(protocolEntry.getKey()))
			.flatMap(v -> v.getValue().stream())
			.collect(Collectors.toSet());

		// Remove WMI for non-windows host
		if (!DeviceKind.WINDOWS.equals(hostType)) {
			sources.remove(WmiSource.class);
		}

		// Add IPMI through WMI
		if (DeviceKind.WINDOWS.equals(hostType) && sources.contains(WmiSource.class)) {
			sources.add(IpmiSource.class);
			// Add OSCommand through Remote WMI Commands
			if (!isLocalhost) {
				sources.add(OsCommandSource.class);
			}
		}

		// Add IPMI through OSCommand remote (SSH)
		if ((DeviceKind.LINUX.equals(hostType) || DeviceKind.SOLARIS.equals(hostType)) && !isLocalhost) {
			sources.add(IpmiSource.class);
		}

		// Handle localhost protocols
		if (isLocalhost) {
			// OS Command always enabled locally
			sources.add(OsCommandSource.class);

			// IPMI executed locally on Linux through OS Command
			if (DeviceKind.LINUX.equals(hostType) || DeviceKind.SOLARIS.equals(hostType)) {
				sources.add(IpmiSource.class);
			}
		}

		return sources;
	}

	/**
	 * Get the protocol configuration used to execute requests on Windows machines.
	 *  (WinRM or WMI)<br> WinRM is prioritized.
	 *
	 * @return {@link IWinConfiguration} instance.
	 */
	public IWinConfiguration getWinConfiguration() {
		// We prioritize WinRM over WMI as it's more efficient.
		final IWinConfiguration protocol = (IWinConfiguration) this.getConfigurations().get(WinRmConfiguration.class);

		// Let's try WMI if the WinRM is not available
		if (protocol == null) {
			return (IWinConfiguration) this.getConfigurations().get(WmiConfiguration.class);
		}

		return protocol;
	}
}
