package com.sentrysoftware.hardware.agent.service.opentelemetry.signal;

import static com.sentrysoftware.hardware.agent.configuration.AgentConfig.*;

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

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import lombok.NonNull;

@SpringBootTest
class OtelSelfObserverTest {

	@Autowired
	private Map<String, String> agentInfo;

	@Test
	void testInit() {
		final Resource resource = OtelHelper
			.createServiceResource(
				agentInfo.get(AGENT_INFO_NAME_ATTRIBUTE_KEY),
				Collections.emptyMap()
			);
		InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = initOpenTelemetryMetrics(resource, inMemoryReader);
	
		MultiHostsConfigurationDto multiHostsConfigurationDto = MultiHostsConfigurationDto
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
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.build()
			.init();

		// Trigger the observer
		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertEquals(4, metrics.size());

		final Map<String, MetricData> metricMap = metrics
				.stream()
				.collect(Collectors.toMap(MetricData::getName, Function.identity()));

		final MetricData agentInfoMetric = metricMap.get("hardware_sentry.agent.info");
		assertNotNull(agentInfoMetric);

		final GaugeData<LongPointData> longGaugeData = agentInfoMetric.getLongGaugeData();
		final LongPointData dataPoint = longGaugeData.getPoints().stream().findAny().orElse(null);
		assertNotNull(dataPoint);
		assertEquals(1, dataPoint.getValue());

		final Attributes attributes = dataPoint.getAttributes();
		assertEquals("Hardware Sentry Agent", attributes.get(AttributeKey.stringKey(AGENT_INFO_NAME_ATTRIBUTE_KEY)));
		assertEquals("2", attributes.get(AttributeKey.stringKey(AGENT_INFO_VERSION_ATTRIBUTE_KEY)));
		assertEquals("80", attributes.get(AttributeKey.stringKey(AGENT_INFO_HC_VERSION_ATTRIBUTE_KEY)));
		assertEquals(
			"Sep 15, 2021 at 5:27:55 PM Central European Summer Time",
			attributes.get(AttributeKey.stringKey(AGENT_INFO_BUILD_DATE_ATTRIBUTE_KEY))
		);
		assertEquals("f9435eed", attributes.get(AttributeKey.stringKey(AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY)));
		assertEquals("0.55.0", attributes.get(AttributeKey.stringKey(AGENT_INFO_OTEL_VERSION_ATTRIBUTE_KEY)));

		assertNotNull(metricMap.get("hw.site.carbon_density_grams"));
		assertNotNull(metricMap.get("hw.site.electricity_cost_dollars"));
		assertNotNull(metricMap.get("hw.site.pue_ratio"));
	}

	/**
	 * Initializes a Metrics SDK with a Resource and an instance of IntervalMetricReader.
	 *
	 * @param resource the resource used for the SdkMeterProvider
	 * @param inMemoryReader the periodic reader running the metrics collect then the OTLP metrics export
	 * @return a ready-to-use {@link SdkMeterProvider} instance
	 */
	public static SdkMeterProvider initOpenTelemetryMetrics(@NonNull final Resource resource,
			@NonNull final InMemoryMetricReader inMemoryReader) {

		return SdkMeterProvider
			.builder()
			.setResource(resource)
			.registerMetricReader(inMemoryReader)
			.build();
	}
}
