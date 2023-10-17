package com.sentrysoftware.metricshub.engine.configuration;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_JOB_TIMEOUT;

import com.sentrysoftware.metricshub.engine.alert.AlertInfo;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostConfiguration {

	private String hostname;
	private String hostId;
	private DeviceKind hostType;

	@Builder.Default
	private long strategyTimeout = DEFAULT_JOB_TIMEOUT;

	private Set<String> selectedConnectors;
	private Set<String> excludedConnectors;
	private boolean sequential;
	private Consumer<AlertInfo> alertTrigger;
	private long retryDelay;
	private Map<String, String> connectorVariables;

	@Builder.Default
	private Map<Class<? extends IConfiguration>, IConfiguration> configurations = new HashMap<>();

	private static final Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> CONFIGURATION_TO_SOURCES_MAP;

	static {
		CONFIGURATION_TO_SOURCES_MAP =
			Map.of(
				SnmpConfiguration.class,
				Set.of(SnmpGetSource.class, SnmpTableSource.class),
				WmiConfiguration.class,
				Collections.singleton(WmiSource.class),
				WbemConfiguration.class,
				Collections.singleton(WbemSource.class),
				SshConfiguration.class,
				Collections.singleton(OsCommandSource.class),
				HttpConfiguration.class,
				Collections.singleton(HttpSource.class),
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
	 * @param isLocalhost Whether the host should be localhost or not
	 * @return {@link Set} of accepted source types
	 */
	public Set<Class<? extends Source>> determineAcceptedSources(final boolean isLocalhost) {
		// protocolConfigurations and host cannot never be null
		final Set<Class<? extends IConfiguration>> protocolTypes = configurations.keySet();

		final Set<Class<? extends Source>> sources = CONFIGURATION_TO_SOURCES_MAP
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
	 * @return {@link IWinProtocol} instance.
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
