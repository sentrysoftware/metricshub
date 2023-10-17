package com.sentrysoftware.metricshub.engine.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class JobInfo {

	private String hostname;
	private String connectorName;
	private String monitorType;
	private String jobName;
}
