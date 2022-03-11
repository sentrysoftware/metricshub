package com.sentrysoftware.matrix.engine;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;

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
				SNMPProtocol.class, Set.of(SNMPGetSource.class, SNMPGetTableSource.class),
				WMIProtocol.class, Collections.singleton(WMISource.class),
				WBEMProtocol.class, Collections.singleton(WBEMSource.class),
				SSHProtocol.class, Set.of(OSCommandSource.class, SshInteractiveSource.class),
				HTTPProtocol.class, Collections.singleton(HTTPSource.class),
				IPMIOverLanProtocol.class, Collections.singleton(IPMI.class),
				OSCommandConfig.class, Collections.singleton(OSCommandSource.class));

	}

	// 5 minutes
	public static final int DEFAULT_JOB_TIMEOUT = 5 * 60;

	private HardwareTarget target;

	@Default
	private Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> protocolConfigurations = new HashMap<>();

	@Default
	private int operationTimeout = DEFAULT_JOB_TIMEOUT;

	@Default
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	private Set<String> excludedConnectors = new HashSet<>();

	private boolean sequential;

	/**
	 * Determine the accepted sources that can be executed using the current engine configuration
	 * 
	 * @param isLocalhost   Whether the target should be localhost or not
	 * @return {@link Set} of accepted source types
	 */
	public Set<Class<? extends Source>> determineAcceptedSources(final boolean isLocalhost) {

		// protocolConfigurations and target cannot never be null
		final Set<Class<? extends IProtocolConfiguration>> protocolTypes = protocolConfigurations.keySet();
		final TargetType targetType = target.getType();

		final Set<Class<? extends Source>> sources = PROTOCOL_TO_SOURCES_MAP
				.entrySet()
				.stream()
				.filter(protocolEntry -> protocolTypes.contains(protocolEntry.getKey()))
				.flatMap(v -> v.getValue().stream())
				.collect(Collectors.toSet());

		// Remove WMI for non-windows target
		if (!TargetType.MS_WINDOWS.equals(targetType)) {
			sources.remove(WMISource.class);
		}

		// Add IPMI through WMI
		if (TargetType.MS_WINDOWS.equals(targetType) && sources.contains(WMISource.class)) {
			sources.add(IPMI.class);
		}

		// Add IPMI through OSCommand remote (SSH)
		if ((TargetType.LINUX.equals(targetType) || TargetType.SUN_SOLARIS.equals(targetType))
				&& protocolTypes.contains(SSHProtocol.class) && !isLocalhost) {
			sources.add(IPMI.class);
		}

		// Handle localhost protocols
		if (isLocalhost) {
			// OS Command always enabled locally
			sources.add(OSCommandSource.class);

			// IPMI executed locally on Linux through OS Command
			if (TargetType.LINUX.equals(targetType) || TargetType.SUN_SOLARIS.equals(targetType)) {
				sources.add(IPMI.class);
			}
		}

		return sources;
	}

}
