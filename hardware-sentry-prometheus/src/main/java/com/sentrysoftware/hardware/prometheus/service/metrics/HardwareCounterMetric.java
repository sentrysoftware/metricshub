package com.sentrysoftware.hardware.prometheus.service.metrics;

import static io.prometheus.client.Collector.Type.COUNTER;

import java.util.List;

import io.prometheus.client.Collector;

public class HardwareCounterMetric extends AbstractHardwareMetricFamily {

	public static final String COUNTER_METRIC_SUFFIX = "_total";

	public HardwareCounterMetric(String name, String help, double value) {
		super(name, COUNTER_METRIC_SUFFIX, COUNTER, help, value);
	}

	public HardwareCounterMetric(String name, String help, List<String> labelNames) {
		super(name, COUNTER_METRIC_SUFFIX, Collector.Type.COUNTER, help, labelNames);
	}

}
