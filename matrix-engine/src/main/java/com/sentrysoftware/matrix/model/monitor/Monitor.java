package com.sentrysoftware.matrix.model.monitor;

import java.util.HashMap;
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

	private String deviceId;
	private String name;
	private MonitorType monitorType;
	private String parentId;
	private String targetId;

	// parameter name to Parameter value
	@Default
	private Map<String, IParameterValue> parameters = new HashMap<>();

}
