package com.sentrysoftware.hardware.agent.service.prometheus;

import static io.prometheus.client.Collector.Type.GAUGE;

import java.util.List;

import io.prometheus.client.Collector.MetricFamilySamples;

/**
 * @deprecated This class builds Prometheus Counter {@link MetricFamilySamples}. This mechanism is
 *             no more supported by the agent
 */
@Deprecated(since = "1.1")
public class HardwareGaugeMetric extends AbstractHardwareMetricFamily {

	public HardwareGaugeMetric(String name, String help, double value) {
		super(name, GAUGE, help, value);
	}

	public HardwareGaugeMetric(String name, String help, List<String> labelNames) {
		super(name, GAUGE, help, labelNames);
	}
}
