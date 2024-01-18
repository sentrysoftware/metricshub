package com.sentrysoftware.metricshub.agent.config;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents the variables associated with a connector. This class is used for storing key-value pairs
 * where keys are variable names and values are the corresponding variable values.
 */
@Data
@AllArgsConstructor
public class ConnectorVariables {

	@Builder.Default
	private Map<String, String> variableValues = new HashMap<>();
}
