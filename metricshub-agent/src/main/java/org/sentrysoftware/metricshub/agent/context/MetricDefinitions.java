package org.sentrysoftware.metricshub.agent.context;

import java.util.Map;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;

/**
 * Represents a collection of metric definitions.
 * @param metrics A map containing metrics. Each metric is identified by its name
 * and contain a {@link MetricDefinition}
 */
public record MetricDefinitions(Map<String, MetricDefinition> metrics) {}
