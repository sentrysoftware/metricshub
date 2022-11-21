package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.hardware.agent.dto.protocol.HttpProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.IpmiOverLanProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.OsCommandConfigDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SshProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WbemProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WinRmProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WmiProtocolDto;
import com.sentrysoftware.matrix.engine.EngineConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO to wrap the agent configuration for one specific host.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostConfigurationDto {

	private HardwareHostDto host;

	private HardwareHostGroupDto hostGroup;
	
	@Default
	private int operationTimeout = EngineConfiguration.DEFAULT_JOB_TIMEOUT;

	private SnmpProtocolDto snmp;

	private IpmiOverLanProtocolDto ipmi;

	private SshProtocolDto ssh;

	private WbemProtocolDto wbem;

	private WmiProtocolDto wmi;

	private HttpProtocolDto http;

	private OsCommandConfigDto osCommand;

	private WinRmProtocolDto winRm;

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	@JsonSetter(nulls = SKIP)
	private Set<String> excludedConnectors = new HashSet<>();

	@JsonDeserialize(using = TimeDeserializer.class)
	private Long collectPeriod;
	private Integer discoveryCycle;

	private String loggerLevel;
	private String outputDirectory;

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> extraLabels = new HashMap<>();

	private Boolean sequential;

	private String hardwareProblemTemplate;

	private Boolean disableAlerts;

	/**
	 * Extract hosts from hostGroup
     *
	 * @return a Set of HostConfigurationDto instances
	 */
	public Set<HostConfigurationDto> resolveHostGroups() {
		Set<String> hostnames = hostGroup.getHostnames().getEntries();
		return hostnames
			.stream()
			.map(hostname -> HostConfigurationDto
				.builder()
				.host(HardwareHostDto
					.builder()
					.hostname(hostname)
					.id(hostname)
					.type(hostGroup.getType())
					.build()
				)
				.operationTimeout(operationTimeout)
				.snmp(snmp)
				.ipmi(ipmi)
				.ssh(ssh)
				.wbem(wbem)
				.wmi(wmi)
				.http(http)
				.osCommand(osCommand)
				.winRm(winRm)
				.selectedConnectors(selectedConnectors)
				.excludedConnectors(excludedConnectors)
				.collectPeriod(collectPeriod)
				.discoveryCycle(discoveryCycle)
				.loggerLevel(loggerLevel)
				.outputDirectory(outputDirectory)
				.extraLabels(mergeExtraLabels(hostname))
				.sequential(sequential)
				.hardwareProblemTemplate(hardwareProblemTemplate)
				.disableAlerts(disableAlerts)
				.build()
			)
			.collect(Collectors.toSet());
	}

	/** 
	 * Merge hostGroup extraLabels with host extraLabels (Priority to host)
     *
	 * @param hostname configured hostname
	 * @return Map of merged extra labels
	 */
	private Map<String, String> mergeExtraLabels(String hostname) {
		final Map<String, String> finalExtraLabels = new HashMap<>();
		finalExtraLabels.putAll(extraLabels);
		Optional<HostnameInfoDto> possibleHostnameInfo = hostGroup.getHostnames().getHostnameInfo(hostname);

		// Get the extra labels from the hostGroups
		if (possibleHostnameInfo.isPresent()) {
			HostnameInfoDto hostnameInfo = possibleHostnameInfo.get();
			Map<String, String> hostnameExtraLabels = hostnameInfo.getExtraLabels();

			// If there are extraLabels in the hostGroup, add them to the map to be returned
			if (hostnameExtraLabels != null) {
				finalExtraLabels.putAll(hostnameExtraLabels);
			}
		}

		return finalExtraLabels;
	}

	/**
	 * 
	 * @return Whether the given hostConfigurationDto is a hostGroup or not.
	 */
	public boolean isHostGroup() {
		return hostGroup != null;
	}
	
	/**
	 * 
	 * @return Whether the given hostConfigurationDto is a single host or not.
	 */
	public boolean isSingleHost() {
		return host != null;
	}
	
}
