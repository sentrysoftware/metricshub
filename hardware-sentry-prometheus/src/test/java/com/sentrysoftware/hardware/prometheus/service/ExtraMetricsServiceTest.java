package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.prometheus.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.prometheus.service.metrics.HardwareGaugeMetric;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;

@ExtendWith(MockitoExtension.class)
class ExtraMetricsServiceTest {

	@Mock
	private MultiHostsConfigurationDTO multiHostsConfigurationDTO;

	@InjectMocks
	private ExtraMetricsService extraMetricsService;

	@Test
	void testBuildExtraMetrics() {

		{
			doReturn(Map.of("site", "Data Center 1", "group", "IT"))
					.when(multiHostsConfigurationDTO).getExtraLabels();
			doReturn(Map.of("hw_carbon_density_grams", 0.66, "hw_electricity_cost_dollars", 0.02))
					.when(multiHostsConfigurationDTO).getExtraMetrics();

			doReturn(true).when(multiHostsConfigurationDTO).isExportTimestamps();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(2, gauges.size());
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(1).samples.get(0).labelValues);
			Sample sample1 = gauges.stream().filter(gauge -> "hw_carbon_density_grams".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0);
			assertEquals(0.66, sample1.value);
			Sample sample2 = gauges.stream().filter(gauge -> "hw_electricity_cost_dollars".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0);
			assertEquals(0.02, sample2.value);
			assertNotNull(sample1.timestampMs);
			assertNotNull(sample2.timestampMs);
		}

		{
			Map<String, String> extraLabels = new HashMap<>();
			extraLabels.put("site", "Data Center 1");
			extraLabels.put("bad_label", null);
			doReturn(extraLabels)
					.when(multiHostsConfigurationDTO).getExtraLabels();
			doReturn(Map.of("hw_carbon_density_grams", 0.66, "hw_electricity_cost_dollars", 0.02))
					.when(multiHostsConfigurationDTO).getExtraMetrics();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(2, gauges.size());
			assertEquals(List.of("", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(List.of("", "Data Center 1"), gauges.get(1).samples.get(0).labelValues);
		}

		{
			final Map<String, Double> extraMetrics = new HashMap<>();
			extraMetrics.put("hw_carbon_density_grams", 0.66); 
			extraMetrics.put("hw_electricity_cost_dollars", null);

			doReturn(Map.of("site", "Data Center 1", "group", "IT"))
					.when(multiHostsConfigurationDTO).getExtraLabels();
			doReturn(extraMetrics)
					.when(multiHostsConfigurationDTO).getExtraMetrics();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(1, gauges.size());
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(0.66, gauges.stream().filter(gauge -> "hw_carbon_density_grams".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0).value);

		}
	}

}
