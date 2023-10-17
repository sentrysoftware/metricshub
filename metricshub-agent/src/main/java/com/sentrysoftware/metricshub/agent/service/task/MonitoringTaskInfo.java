package com.sentrysoftware.metricshub.agent.service.task;

import com.sentrysoftware.metricshub.agent.config.ResourceConfig;
import com.sentrysoftware.metricshub.agent.context.MetricDefinitions;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
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
