package com.sentrysoftware.matrix.configuration;

import com.sentrysoftware.matrix.alert.AlertInfo;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.DEFAULT_JOB_TIMEOUT;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostConfiguration {

	private String hostname;
	private String hostId;
	private DeviceKind hostType;
	@Builder.Default
	private long strategyTimeout = DEFAULT_JOB_TIMEOUT;
	private Set<String> excludedConnectors;
	private boolean sequential;
	private Consumer<AlertInfo> alertTrigger;
	private long retryDelay;
	private Map<String, String> connectorVariables;
	private Map<Class<? extends IConfiguration>, IConfiguration> configurations;
}
