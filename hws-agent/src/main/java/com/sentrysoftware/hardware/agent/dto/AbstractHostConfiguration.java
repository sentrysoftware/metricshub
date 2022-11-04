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

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractHostConfiguration {
	
	protected int operationTimeout = EngineConfiguration.DEFAULT_JOB_TIMEOUT;

	protected SnmpProtocolDto snmp;

	protected IpmiOverLanProtocolDto ipmi;

	protected SshProtocolDto ssh;

	protected WbemProtocolDto wbem;

	protected WmiProtocolDto wmi;

	protected HttpProtocolDto http;

	protected OsCommandConfigDto osCommand;

	protected WinRmProtocolDto winRm;

	@JsonSetter(nulls = SKIP)
	protected Set<String> selectedConnectors = new HashSet<>();

	@JsonSetter(nulls = SKIP)
	protected Set<String> excludedConnectors = new HashSet<>();

	@JsonDeserialize(using = TimeDeserializer.class)
	protected Long collectPeriod;
	protected Integer discoveryCycle;

	protected String loggerLevel = "OFF";

	protected String outputDirectory = DEFAULT_OUTPUT_DIRECTORY.toString();

	@JsonSetter(nulls = SKIP)
	protected Map<String, String> extraLabels = new HashMap<>();

	protected Boolean sequential;

	protected String hardwareProblemTemplate;

	protected Boolean disableAlerts;
	
	protected AbstractHostConfiguration( // NOSONAR on DTO
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
		Boolean disableAlerts
	) {
		this.operationTimeout = operationTimeout > 0 ? operationTimeout : EngineConfiguration.DEFAULT_JOB_TIMEOUT;
		this.snmp = snmp;
		this.ipmi = ipmi;
		this.ssh = ssh;
		this.wbem = wbem;
		this.wmi = wmi;
		this.http = http;
		this.osCommand = osCommand;
		this.winRm = winRm;
		this.selectedConnectors = selectedConnectors != null ? selectedConnectors : new HashSet<>();
		this.excludedConnectors = excludedConnectors != null ? excludedConnectors : new HashSet<>();
		this.collectPeriod = collectPeriod;
		this.discoveryCycle = discoveryCycle;
		this.loggerLevel = loggerLevel;
		this.outputDirectory = outputDirectory;
		this.extraLabels = extraLabels != null ? extraLabels : new HashMap<>();
		this.sequential = sequential;
		this.hardwareProblemTemplate = hardwareProblemTemplate;
		this.disableAlerts = disableAlerts;
	}
	
	
}
