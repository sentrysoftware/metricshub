package com.sentrysoftware.hardware.agent.service.prometheus;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

/***
 * @deprecated This service export metrics in Prometheus format. This mechanism is
 *             no more supported by the agent
 */
@Service
@Deprecated(since = "1.1")
public class ExtraMetricsService {

	@Autowired
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	/**
	 * Build user configured extra metrics. The generated gauge metrics include the configured extra labels.
	 * 
	 * @return List of {@link HardwareGaugeMetric}s
	 */
	public List<HardwareGaugeMetric> buildExtraMetrics() {

		final List<String> labelKeys = multiHostsConfigurationDto
				.getExtraLabels()
				.keySet()
				.stream()
				.sorted()
				.collect(Collectors.toList());

		final List<String> labelValues = labelKeys
				.stream()
				.map(labelKey -> multiHostsConfigurationDto.getExtraLabels().getOrDefault(labelKey, ""))
				.collect(Collectors.toList());

		return multiHostsConfigurationDto
				.getExtraMetrics()
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue() != null)
				.map(entry -> createGauge(labelKeys, labelValues, entry.getKey(), entry.getValue()))
				.collect(Collectors.toList());

	}

	/**
	 * Create a new gauge metric
	 * 
	 * @param labelKeys   metric label keys
	 * @param labelValues metric label values
	 * @param metricName  metric name
	 * @param metricValue metric Double value
	 * @return new {@link HardwareGaugeMetric} metric
	 */
	private HardwareGaugeMetric createGauge(final List<String> labelKeys, final List<String> labelValues,
			final String metricName, final Double metricValue) {

		// Create the instance
		final HardwareGaugeMetric metric = new HardwareGaugeMetric(metricName, "", labelKeys);

		// Add an new metric sample
		metric.addMetric(
				labelValues,
				metricValue,
				multiHostsConfigurationDto.isExportTimestamps() ? new Date().getTime() : null);

		return metric;
	}

}
