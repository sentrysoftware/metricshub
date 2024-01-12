package com.sentrysoftware.metricshub.agent.config;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnectorVariables {

	@Default
	private Map<String, String> variableValues = new HashMap<>();

	/**
	 * Add a variable value in the map.
	 *
	 * @param name  The variable name.
	 * @param value The variable value.
	 */
	public void addVariableValue(final String name, final String value) {
		variableValues.put(name, value);
	}
}
