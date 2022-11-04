package com.sentrysoftware.hardware.agent.dto;

import java.util.Map;
import java.util.Set;

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
 * DTO to wrap the agent configuration for one specific host.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class HostConfigurationDto extends AbstractHostConfiguration {

	@NonNull
	private HardwareHostDto host;

	private boolean generated;

	@Builder
	public HostConfigurationDto(
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
		HardwareHostDto host,
		boolean generated
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

		if (host == null) {
			throw new IllegalArgumentException("host field is marked non-null but is null.");
		}

		this.host = host;
		this.generated = generated;
	}
}
