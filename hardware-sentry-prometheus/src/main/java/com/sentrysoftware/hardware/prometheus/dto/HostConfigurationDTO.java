package com.sentrysoftware.hardware.prometheus.dto;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.CiscoUcsProtocol;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;
import com.sentrysoftware.matrix.engine.protocol.IPMIOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
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
	private HardwareTarget target;

	@Default
	private int operationTimeout = EngineConfiguration.DEFAULT_JOB_TIMEOUT;

	private SNMPProtocol snmp;

	private CiscoUcsProtocol ciscoUcs;

	private IPMIOverLanProtocol ipmi;

	private SSHProtocol ssh;

	private WBEMProtocol wbem;

	private WMIProtocol wmi;

	private HTTPProtocol http;

	private OSCommandConfig osCommand;

	@Default
	private Set<String> selectedConnectors = new HashSet<>();

	@Default
	private Set<String> excludedConnectors = new HashSet<>();

	@Default
	private ParameterState unknownStatus = ParameterState.WARN;
}
