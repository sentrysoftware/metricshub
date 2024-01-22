package org.sentrysoftware.metricshub.agent.service.task;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.sentrysoftware.metricshub.agent.config.ResourceConfig;
import org.sentrysoftware.metricshub.agent.context.MetricDefinitions;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Information required for the monitoring task, including telemetry manager, resource configuration, keys, OpenTelemetry SDK configuration, and host metric definitions.
 */
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
