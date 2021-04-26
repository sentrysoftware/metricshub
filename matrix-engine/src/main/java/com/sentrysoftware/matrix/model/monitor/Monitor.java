package com.sentrysoftware.matrix.model.monitor;

import java.util.TreeMap;
import java.util.Map;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Monitor {

	private String id;
	private String name;
	private MonitorType monitorType;
	private String parentId;
	private String targetId;
	private String extendedType;

	// parameter name to Parameter value
	@Default
	private Map<String, IParameterValue> parameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	public void addParameter(IParameterValue parameter) {
		parameters.put(parameter.getName(), parameter);
	}
}
