package com.sentrysoftware.hardware.prometheus.dto;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.UnknownStatusDeserializer;
import com.sentrysoftware.hardware.prometheus.dto.protocol.HttpProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.IpmiOverLanProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.OsCommandConfigDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.SnmpProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.SshProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.WbemProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.WmiProtocolDTO;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * DTO to wrap the exporter configuration for one specific target.
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
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	private Set<String> excludedConnectors = new HashSet<>();

	@Default
	@JsonDeserialize(using = UnknownStatusDeserializer.class)
	private Optional<ParameterState> unknownStatus = Optional.of(ParameterState.WARN);

	private Integer collectPeriod;
	private Integer discoveryCycle;
}
