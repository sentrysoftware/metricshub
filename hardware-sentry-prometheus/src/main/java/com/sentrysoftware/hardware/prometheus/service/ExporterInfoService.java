package com.sentrysoftware.hardware.prometheus.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.service.metrics.HardwareGaugeMetric;

import io.prometheus.client.Collector.MetricFamilySamples;

@Service
public class ExporterInfoService {

	@Autowired
	private MultiHostsConfigurationDTO multiHostsConfigurationDTO;

	@Autowired
	private Map<String, String> exporterInfo;

	/**
	 * Build the hw_exporter_info metric to report the exporter information as
	 * labels: projectName, projectVersion, buildNumber, timestamp, hcVersion and the extraLabels
	 * 
	 * @return hw_exporter_info {@link MetricFamilySamples} metric
	 */
	public MetricFamilySamples buildExporterInfoMetric() {

		final List<String> labelKeys = Stream
				.concat(exporterInfo.keySet().stream(), multiHostsConfigurationDTO.getExtraLabels().keySet().stream())
				.sorted()
				.collect(Collectors.toList());

		final HardwareGaugeMetric metric = new HardwareGaugeMetric(
				"hw_exporter_info",
				"Reports exporter information",
				labelKeys
		);

		metric.addMetric(
				buildLabelValues(labelKeys),
				1D,
				multiHostsConfigurationDTO.isExportTimestamps() ? new Date().getTime() : null);

		return metric;
	}

	/**
	 * Build the label values for the given label keys. Note that the label keys may
	 * include extra labels, do this method will include the extra label values in the
	 * final label values
	 * 
	 * @param labelKeys
	 * @return List of String values
	 */
	private List<String> buildLabelValues(final List<String> labelKeys) {
		return labelKeys.stream()
				.map(labelKey -> exporterInfo.getOrDefault(labelKey,
						multiHostsConfigurationDTO.getExtraLabels().getOrDefault(labelKey, "")))
				.collect(Collectors.toList());
	}
}
