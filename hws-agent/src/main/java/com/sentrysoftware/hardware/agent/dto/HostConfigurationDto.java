package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
import lombok.NonNull;

/**
 * DTO to wrap the agent configuration for one specific host.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostConfigurationDto {

	@NonNull
	private HardwareHostDto host;

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

	@Default
	private String loggerLevel = "OFF";

	@Default
	private String outputDirectory = DEFAULT_OUTPUT_DIRECTORY.toString();

	@Default
	@JsonSetter(nulls = SKIP)
	private Map<String, String> extraLabels = new HashMap<>();

	private Boolean sequential;

	private String hardwareProblemTemplate;

	private Boolean disableAlerts;
}
