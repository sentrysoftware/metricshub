package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

@SpringBootTest
class OtelSelfObserverTest {

	@Autowired
	private Map<String, String> agentInfo;

	@Test
	void testInit() {
		final Resource resource = OtelHelper.createServiceResource(agentInfo.get("project_name"));
		InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = OtelHelper.initOpenTelemetryMetrics(resource, inMemoryReader);
	
		MultiHostsConfigurationDTO multiHostsConfigurationDTO = MultiHostsConfigurationDTO
				.builder()
				.extraMetrics(Map.of(
								"hw.site.carbon_density_grams", 350D,
								"hw.site.electricity_cost_dollars", 0.12D,
								"hw.site.pue_ratio", 1.8D
							 )
				).build();

		OtelSelfObserver
			.builder()
			.agentInfo(agentInfo)
			.sdkMeterProvider(sdkMeterProvider)
			.multiHostsConfigurationDTO(multiHostsConfigurationDTO)
			.build()
			.init();

		// Trigger the observer
		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertEquals(4, metrics.size());

		final Map<String, MetricData> metricMap = metrics
				.stream()
				.collect(Collectors.toMap(MetricData::getName, Function.identity()));

		assertNotNull(metricMap.get("hw.agent.info"));
		assertNotNull(metricMap.get("hw.site.carbon_density_grams"));
		assertNotNull(metricMap.get("hw.site.electricity_cost_dollars"));
		assertNotNull(metricMap.get("hw.site.pue_ratio"));
	}

}
