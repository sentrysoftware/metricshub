package com.sentrysoftware.hardware.prometheus.service.metrics;

import static io.prometheus.client.Collector.Type.GAUGE;

import java.util.List;

public class HardwareGaugeMetric extends AbstractHardwareMetricFamily {

	public HardwareGaugeMetric(String name, String help, double value) {
		super(name, GAUGE, help, value);
	}

	public HardwareGaugeMetric(String name, String help, List<String> labelNames) {
		super(name, GAUGE, help, labelNames);
	}
}
