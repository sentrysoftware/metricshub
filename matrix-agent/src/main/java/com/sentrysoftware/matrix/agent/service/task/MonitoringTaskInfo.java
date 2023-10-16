package com.sentrysoftware.matrix.agent.service.task;

import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.context.MetricDefinitions;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
@Builder
public class MonitoringTaskInfo {

	@NonNull
	private TelemetryManager telemetryManager;

	@NonNull
	private ResourceConfig resourceConfig;

	@NonNull
	private String resourceKey;

	@NonNull
	private String resourceGroupKey;

	@NonNull
	private Map<String, String> otelSdkConfiguration;

	@NonNull
	private MetricDefinitions hostMetricDefinitions;
}
