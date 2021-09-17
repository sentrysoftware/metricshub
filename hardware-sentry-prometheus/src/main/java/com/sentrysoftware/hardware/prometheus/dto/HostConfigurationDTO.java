package com.sentrysoftware.hardware.prometheus.dto;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.hardware.prometheus.dto.protocol.HTTPProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.IPMIOverLanProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.OSCommandConfigDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.SSHProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.SnmpProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.WBEMProtocolDTO;
import com.sentrysoftware.hardware.prometheus.dto.protocol.WMIProtocolDTO;
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

	private IPMIOverLanProtocolDTO ipmi;

	private SSHProtocolDTO ssh;

	private WBEMProtocolDTO wbem;

	private WMIProtocolDTO wmi;

	private HTTPProtocolDTO http;

	private OSCommandConfigDTO osCommand;

	@Default
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	private Set<String> excludedConnectors = new HashSet<>();

	@Default
	private ParameterState unknownStatus = ParameterState.WARN;
}
