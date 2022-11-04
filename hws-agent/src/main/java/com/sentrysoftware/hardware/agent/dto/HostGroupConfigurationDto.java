package com.sentrysoftware.hardware.agent.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.hardware.agent.dto.protocol.HttpProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.IpmiOverLanProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.OsCommandConfigDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SshProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WbemProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WinRmProtocolDto;
import com.sentrysoftware.hardware.agent.dto.protocol.WmiProtocolDto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

/**
 * DTO to wrap the agent configuration for one specific host group.
 */

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HostGroupConfigurationDto extends AbstractHostConfiguration{
	@NonNull
	private HardwareHostGroupDto hostGroup;

	@Builder
	public HostGroupConfigurationDto(
		int operationTimeout,
		SnmpProtocolDto snmp,
		IpmiOverLanProtocolDto ipmi,
		SshProtocolDto ssh,
		WbemProtocolDto wbem,
		WmiProtocolDto wmi,
		HttpProtocolDto http,
		OsCommandConfigDto osCommand,
		WinRmProtocolDto winRm,
		Set<String> selectedConnectors,
		Set<String> excludedConnectors,
		Long collectPeriod,
		Integer discoveryCycle,
		String loggerLevel,
		String outputDirectory, 
		Map<String, String> extraLabels,
		Boolean sequential,
		String hardwareProblemTemplate,
		Boolean disableAlerts,
		HardwareHostGroupDto hostGroup
	) {
		super(
			operationTimeout,
			snmp,
			ipmi,
			ssh,
			wbem,
			wmi,
			http,
			osCommand,
			winRm,
			selectedConnectors,
			excludedConnectors,
			collectPeriod,
			discoveryCycle,
			loggerLevel,
			outputDirectory,
			extraLabels,
			sequential,
			hardwareProblemTemplate,
			disableAlerts
		);

		if (hostGroup == null) {
			throw new IllegalArgumentException("hostGroup field is marked non-null but is null.");
		}

		this.hostGroup = hostGroup;
	}
	/** Extract hosts from hostGroup
	 * 
	 * @return
	 */
	public Set<HostConfigurationDto> toHosts() {
		Set<String> hostnames = hostGroup.getHostnames().getEntries();
		return hostnames
			.stream()
			.map(hostname -> HostConfigurationDto
				.builder()
				.host(HardwareHostDto
					.builder()
					.hostname(hostname)
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
				.generated(true)
				.build()
			)
			.collect(Collectors.toSet());
	}

	/** Merge hostGroup extraLabels with host extraLabels (Priority to host)
	 * 
	 * @param hostname
	 * @return
	 */
	private Map<String, String> mergeExtraLabels(String hostname) {
		final Map<String, String> finalExtraLabels = new HashMap<>();
		finalExtraLabels.putAll(extraLabels);
		Optional<HostnameInfoDto> possibleHostnameInfo = hostGroup.getHostnames().getHostnameInfo(hostname);
		if (possibleHostnameInfo.isPresent()) {
			HostnameInfoDto hostnameInfo = possibleHostnameInfo.get();
			Map<String, String> hostnameExtraLabels = hostnameInfo.getExtraLabels();
			if (hostnameExtraLabels != null) {
				finalExtraLabels.putAll(hostnameExtraLabels);
			}
		}

		return finalExtraLabels;
	}
}
