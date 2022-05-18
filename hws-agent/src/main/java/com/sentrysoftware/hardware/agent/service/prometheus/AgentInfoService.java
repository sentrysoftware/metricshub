package com.sentrysoftware.hardware.agent.service.prometheus;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

import io.prometheus.client.Collector.MetricFamilySamples;

/**
 * @deprecated This service exports metrics in Prometheus format. This mechanism is
 *             no more supported by the agent
 */
@Service
@Deprecated(since = "1.1")
public class AgentInfoService {

	@Autowired
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Autowired
	private Map<String, String> agentInfo;

	/**
	 * Build the hw_agent_info metric to report the exporter information as
	 * labels: projectName, projectVersion, buildNumber, timestamp, hcVersion and the extraLabels
	 * 
	 * @return hw_agent_info {@link MetricFamilySamples} metric
	 */
	public MetricFamilySamples buildAgentInfoMetric() {

		final List<String> labelKeys = Stream
				.concat(agentInfo.keySet().stream(), multiHostsConfigurationDto.getExtraLabels().keySet().stream())
				.sorted()
				.collect(Collectors.toList());

		final HardwareGaugeMetric metric = new HardwareGaugeMetric(
				"hw_agent_info",
				"Reports agent information",
				labelKeys
		);

		metric.addMetric(
				buildLabelValues(labelKeys),
				1D,
				multiHostsConfigurationDto.isExportTimestamps() ? new Date().getTime() : null);

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
				.map(labelKey -> agentInfo.getOrDefault(labelKey,
						multiHostsConfigurationDto.getExtraLabels().getOrDefault(labelKey, "")))
				.collect(Collectors.toList());
	}
}
