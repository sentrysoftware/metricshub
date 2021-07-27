package com.sentrysoftware.matrix.engine;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

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
	private ParameterState unknownStatus = ParameterState.WARN;
}
