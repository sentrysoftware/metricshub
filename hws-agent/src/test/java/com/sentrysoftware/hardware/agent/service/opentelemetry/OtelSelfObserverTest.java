package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import lombok.NonNull;

@SpringBootTest
class OtelSelfObserverTest {

	@Autowired
	private Map<String, String> agentInfo;

	@Test
	void testInit() {
		final Resource resource = OtelHelper.createServiceResource(agentInfo.get("project_name"), Collections.emptyMap());
		InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = initOpenTelemetryMetrics(resource, inMemoryReader);
	
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

	/**
	 * Initializes a Metrics SDK with a Resource and an instance of IntervalMetricReader.
	 *
	 * @param resource the resource used for the SdkMeterProvider
	 * @param periodicReaderFactory the periodic reader running the metrics collect then the OTLP metrics export
	 * @return a ready-to-use {@link SdkMeterProvider} instance
	 */
	public static SdkMeterProvider initOpenTelemetryMetrics(@NonNull final Resource resource,
			@NonNull final MetricReaderFactory periodicReaderFactory) {

		return SdkMeterProvider.builder()
					.setResource(resource)
					.registerMetricReader(periodicReaderFactory)
					.build();
	}
}
