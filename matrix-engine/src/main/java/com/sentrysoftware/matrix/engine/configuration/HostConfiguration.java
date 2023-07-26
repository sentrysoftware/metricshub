package com.sentrysoftware.matrix.engine.configuration;

import com.sentrysoftware.matrix.connector.model.alert.AlertInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HostConfiguration {
	private String hostname;
	private String hostType;
	private long strategyTimeout;
	private Set<String> excludedConnectors;
	private boolean sequential;
	private Consumer<AlertInfo> alertTrigger;
	private long retryDelay;
	private Map<String, String> connectorVariables;
	private Map<Class<? extends IConfiguration>, IConfiguration> configurations;
}
