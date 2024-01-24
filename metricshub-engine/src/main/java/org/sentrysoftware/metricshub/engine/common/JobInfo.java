package org.sentrysoftware.metricshub.engine.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents information about a job, including the hostname, connector ID, monitor type, and job name.
 */
@Builder
@Data
@AllArgsConstructor
public class JobInfo {

	private String hostname;
	private String connectorId;
	private String monitorType;
	private String jobName;
}
