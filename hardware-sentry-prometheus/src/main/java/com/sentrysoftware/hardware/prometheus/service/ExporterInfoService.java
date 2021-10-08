package com.sentrysoftware.hardware.prometheus.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.service.metrics.HardwareGaugeMetric;

import io.prometheus.client.Collector.MetricFamilySamples;

@Service
public class ExporterInfoService {

	protected static final List<String> LABELS = Arrays.asList("build_number", "hc_version", "project_name", "project_version", "timestamp");

	// These properties come from src/main/resources/application.yml or application-ssl.yml
	// which themselves are "filtered" by Maven's resources plugin to expose
	// pom.xml's values
	@Value("${project.name}")
	String projectName;

	@Value("${project.version}")
	String projectVersion;

	@Value("${buildNumber}")
	String buildNumber;

	@Value("${timestamp}")
	String timestamp;

	@Value("${hcVersion}")
	String hcVersion;

	@Autowired
	private MultiHostsConfigurationDTO multiHostsConfigurationDTO;

	/**
	 * Build the hw_exporter_info metric to report the exporter information as
	 * labels: projectName, projectVersion, buildNumber, timestamp, hcVersion
	 * 
	 * @return hw_exporter_info {@link MetricFamilySamples} metric
	 */
	public MetricFamilySamples getExporterInfoMetric() {
		final HardwareGaugeMetric metric = new HardwareGaugeMetric("hw_exporter_info", "Reports exporter information", LABELS);
		metric.addMetric(
				Arrays.asList(buildNumber, hcVersion, projectName, projectVersion, timestamp),
				1,
				multiHostsConfigurationDTO.isExportTimestamps() ? new Date().getTime() : null);
		return metric;
	}

}
