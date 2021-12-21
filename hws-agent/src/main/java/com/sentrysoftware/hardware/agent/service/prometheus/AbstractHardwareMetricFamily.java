package com.sentrysoftware.hardware.agent.service.prometheus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.Type;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * @deprecated This class builds Prometheus {@link MetricFamilySamples}. This mechanism is
 *             no more supported by the agent
 */
@EqualsAndHashCode(callSuper = true)
@Deprecated(since = "1.1")
public abstract class AbstractHardwareMetricFamily extends Collector.MetricFamilySamples {

	@Getter
	private final List<String> labelNames;
	@Getter
	private final String nameSuffix;

	protected AbstractHardwareMetricFamily(String name, String nameSuffix, Type type, String help, List<String> labelNames) {
		super(name, type, help, new ArrayList<>());
		this.labelNames = labelNames;
		this.nameSuffix = nameSuffix;
	}

	protected AbstractHardwareMetricFamily(String name, String nameSuffix, Type type, String help, double value) {
		this(name, nameSuffix, type, help, Collections.emptyList());
		samples.add(new Sample(this.name + this.nameSuffix, labelNames, Collections.emptyList(), value));
	}

	protected AbstractHardwareMetricFamily(String name, Type type, String help, double value) {
		this(name, "", type, help, value);
	}

	protected AbstractHardwareMetricFamily(String name, Type type, String help, List<String> labelNames) {
		this(name, "", type, help, labelNames);
	}

	/**
	 * Add a metric sample with timestamp
	 * 
	 * @param labelValues metric label values, must have the same size as the
	 *                    predefined label names
	 * @param value       the value to set in the new sample
	 * @param timestampMs the time stamp of the metric value
	 * @return this {@link HardwareCounterMetric}
	 */
	public AbstractHardwareMetricFamily addMetric(List<String> labelValues, Double value, Long timestampMs) {
		Assert.isTrue(labelValues.size() == labelNames.size(), "Incorrect number of labels.");

		if (value != null) {
			labelValues = labelValues
					.stream()
					.map(labelValue -> labelValue != null ? labelValue : "")
					.collect(Collectors.toList());

			samples.add(new Sample(name + this.nameSuffix, labelNames, labelValues, value, timestampMs));
		}

		return this;
	}
}
