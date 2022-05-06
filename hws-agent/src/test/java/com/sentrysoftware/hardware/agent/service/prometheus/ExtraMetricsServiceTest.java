package com.sentrysoftware.hardware.agent.service.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

import io.prometheus.client.Collector.MetricFamilySamples.Sample;

@ExtendWith(MockitoExtension.class)
@Deprecated(since = "1.1")
class ExtraMetricsServiceTest {

	@Mock
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@InjectMocks
	private ExtraMetricsService extraMetricsService;

	@Test
	void testBuildExtraMetrics() {

		{
			doReturn(Map.of("site", "Data Center 1", "group", "IT"))
					.when(multiHostsConfigurationDto).getExtraLabels();
			doReturn(Map.of("hw_carbon_density_grams", 0.66, "hw_electricity_cost_dollars", 0.02))
					.when(multiHostsConfigurationDto).getExtraMetrics();

			doReturn(true).when(multiHostsConfigurationDto).isExportTimestamps();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(2, gauges.size());
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(1).samples.get(0).labelValues);
			final Sample sample1 = gauges.stream().filter(gauge -> "hw_carbon_density_grams".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0);
			assertEquals(0.66, sample1.value);
			final Sample sample2 = gauges.stream().filter(gauge -> "hw_electricity_cost_dollars".equals(gauge.name))
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
					.when(multiHostsConfigurationDto).getExtraLabels();
			doReturn(Map.of("hw_carbon_density_grams", 0.66, "hw_electricity_cost_dollars", 0.02))
					.when(multiHostsConfigurationDto).getExtraMetrics();

			doReturn(false).when(multiHostsConfigurationDto).isExportTimestamps();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(2, gauges.size());
			assertEquals(List.of("", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(List.of("", "Data Center 1"), gauges.get(1).samples.get(0).labelValues);

			final Sample sample1 = gauges.stream().filter(gauge -> "hw_carbon_density_grams".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0);
			assertEquals(0.66, sample1.value);
			final Sample sample2 = gauges.stream().filter(gauge -> "hw_electricity_cost_dollars".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0);
			assertEquals(0.02, sample2.value);
			assertNull(sample1.timestampMs);
			assertNull(sample2.timestampMs);
		}

		{
			final Map<String, Double> extraMetrics = new HashMap<>();
			extraMetrics.put("hw_carbon_density_grams", 0.66); 
			extraMetrics.put("hw_electricity_cost_dollars", null);

			doReturn(Map.of("site", "Data Center 1", "group", "IT"))
					.when(multiHostsConfigurationDto).getExtraLabels();
			doReturn(extraMetrics)
					.when(multiHostsConfigurationDto).getExtraMetrics();

			final List<HardwareGaugeMetric> gauges = extraMetricsService.buildExtraMetrics();

			assertEquals(1, gauges.size());
			assertEquals(List.of("IT", "Data Center 1"), gauges.get(0).samples.get(0).labelValues);
			assertEquals(0.66, gauges.stream().filter(gauge -> "hw_carbon_density_grams".equals(gauge.name))
					.findFirst().orElseThrow().samples.get(0).value);

		}
	}

}
