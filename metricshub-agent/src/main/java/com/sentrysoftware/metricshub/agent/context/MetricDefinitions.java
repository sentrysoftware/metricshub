package com.sentrysoftware.metricshub.agent.context;

import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import java.util.Map;

/**
 * Represents a collection of metric definitions.
 * @param metrics A map containing metrics. Each metric is identified by its name
 * and contain a {@link MetricDefinition}
 */
public record MetricDefinitions(Map<String, MetricDefinition> metrics) {}
