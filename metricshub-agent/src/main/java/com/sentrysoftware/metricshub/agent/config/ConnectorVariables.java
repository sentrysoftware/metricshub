package com.sentrysoftware.metricshub.agent.config;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConnectorVariables {

	@Builder.Default
	private Map<String, String> variableValues = new HashMap<>();
}
