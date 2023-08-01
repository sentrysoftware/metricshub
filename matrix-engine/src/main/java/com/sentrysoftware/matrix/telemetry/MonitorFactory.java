package com.sentrysoftware.matrix.telemetry;

import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonitorFactory {

	private Map<String, AbstractMetric> metrics;

	private Map<String, String> attributes;

	private Resource resource;

	private Map<String, List<AlertRule>> alertRules;

	private TelemetryManager telemetryManager;
}
