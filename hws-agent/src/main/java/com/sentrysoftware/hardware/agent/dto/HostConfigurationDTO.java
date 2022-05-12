package com.sentrysoftware.hardware.agent.dto;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.hardware.agent.configuration.ConfigHelper.DEFAULT_OUTPUT_DIRECTORY;

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.hardware.agent.dto.protocol.HttpProtocolDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.IpmiOverLanProtocolDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.OsCommandConfigDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.SshProtocolDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.WbemProtocolDTO;
import com.sentrysoftware.hardware.agent.dto.protocol.WmiProtocolDTO;
import com.sentrysoftware.matrix.engine.EngineConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * DTO to wrap the agent configuration for one specific target.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostConfigurationDTO {

	@NonNull
	private HardwareTargetDTO target;

	@Default
	private int operationTimeout = EngineConfiguration.DEFAULT_JOB_TIMEOUT;

	private SnmpProtocolDTO snmp;

	private IpmiOverLanProtocolDTO ipmi;

	private SshProtocolDTO ssh;

	private WbemProtocolDTO wbem;

	private WmiProtocolDTO wmi;

	private HttpProtocolDTO http;

	private OsCommandConfigDTO osCommand;

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
}
