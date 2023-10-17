package com.sentrysoftware.metricshub.agent.context;

import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import java.util.Map;

public record MetricDefinitions(Map<String, MetricDefinition> metrics) {}
