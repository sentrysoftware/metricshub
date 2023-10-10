package com.sentrysoftware.matrix.agent.context;

import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import java.util.Map;

public record MetricDefinitions(Map<String, MetricDefinition> metrics) {}
