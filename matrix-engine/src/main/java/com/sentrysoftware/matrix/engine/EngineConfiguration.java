package com.sentrysoftware.matrix.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IWinProtocol;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WinRmProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.model.alert.AlertInfo;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EngineConfiguration {

	private static final Map<Class<? extends IProtocolConfiguration>, Set<Class<? extends Source>>> PROTOCOL_TO_SOURCES_MAP;

	static {

		PROTOCOL_TO_SOURCES_MAP = Map.of(
				SnmpProtocol.class, Set.of(SnmpGetSource.class, SnmpGetTableSource.class),
				WmiProtocol.class, Collections.singleton(WmiSource.class),
				WbemProtocol.class, Collections.singleton(WbemSource.class),
				SshProtocol.class, Set.of(OsCommandSource.class, SshInteractiveSource.class),
				HttpProtocol.class, Collections.singleton(HttpSource.class),
				IpmiOverLanProtocol.class, Collections.singleton(Ipmi.class),
				OsCommandConfig.class, Collections.singleton(OsCommandSource.class),
				WinRmProtocol.class, Collections.singleton(WmiSource.class));

	}

	// 5 minutes
	public static final int DEFAULT_JOB_TIMEOUT = 5 * 60;

	private HardwareHost host;

	@Default
	private Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();

	@Default
	private int operationTimeout = DEFAULT_JOB_TIMEOUT;

	@Default
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	private Set<String> excludedConnectors = new HashSet<>();

	private boolean sequential;

	private Consumer<AlertInfo> alertTrigger;

	/**
	 * Determine the accepted sources that can be executed using the current engine configuration
	 * 
	 * @param isLocalhost   Whether the host should be localhost or not
	 * @return {@link Set} of accepted source types
	 */
	public Set<Class<? extends Source>> determineAcceptedSources(final boolean isLocalhost) {

		// protocolConfigurations and host cannot never be null
		final Set<Class<? extends IProtocolConfiguration>> protocolTypes = protocolConfigurations.keySet();
		final HostType hostType = host.getType();

		final Set<Class<? extends Source>> sources = PROTOCOL_TO_SOURCES_MAP
				.entrySet()
				.stream()
				.filter(protocolEntry -> protocolTypes.contains(protocolEntry.getKey()))
				.flatMap(v -> v.getValue().stream())
				.collect(Collectors.toSet());

		// Remove WMI for non-windows host
		if (!HostType.MS_WINDOWS.equals(hostType)) {
			sources.remove(WmiSource.class);
		}

		// Add IPMI through WMI
		if (HostType.MS_WINDOWS.equals(hostType) && sources.contains(WmiSource.class)) {
			sources.add(Ipmi.class);
		}

		// Add IPMI through OSCommand remote (SSH)
		if ((HostType.LINUX.equals(hostType) || HostType.SUN_SOLARIS.equals(hostType))
				&& protocolTypes.contains(SshProtocol.class) && !isLocalhost) {
			sources.add(Ipmi.class);
		}

		// Handle localhost protocols
		if (isLocalhost) {
			// OS Command always enabled locally
			sources.add(OsCommandSource.class);

			// IPMI executed locally on Linux through OS Command
			if (HostType.LINUX.equals(hostType) || HostType.SUN_SOLARIS.equals(hostType)) {
				sources.add(Ipmi.class);
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
	public IWinProtocol getWinProtocol() {
		// We prioritize WinRM over WMI as it's more efficient.
		final IWinProtocol protocol = (WinRmProtocol) this.getProtocolConfigurations().get(WinRmProtocol.class);

		// Let's try WMI if the WinRM is not available
		if (protocol == null) {
			return (WmiProtocol) this.getProtocolConfigurations().get(WmiProtocol.class);
		}

		return protocol;
	}

}
