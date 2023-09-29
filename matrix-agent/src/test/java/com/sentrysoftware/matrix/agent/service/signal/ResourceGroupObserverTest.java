package com.sentrysoftware.matrix.agent.service.signal;

import static com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver.HW_SITE_CARBON_INTENSITY_METRIC;
import static com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver.HW_SITE_ELECTRICITY_COST_METRIC;
import static com.sentrysoftware.matrix.agent.service.signal.ResourceGroupMetricsObserver.HW_SITE_PUE_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.context.AgentInfo;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.agent.helper.OtelTestHelper;
import com.sentrysoftware.matrix.agent.helper.TestConstants;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ResourceGroupObserverTest {

	private static final String SITE_ATTRIBUTE_KEY = "site";
	private static final AttributeKey<String> SITE_OTEL_ATTRIBUTE_KEY = AttributeKey.stringKey(SITE_ATTRIBUTE_KEY);

	@Test
	void testInit() {
		final AgentInfo agentInfo = new AgentInfo();
		final Resource resource = OtelHelper.createServiceResource(agentInfo.getResourceAttributes());
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = OtelTestHelper.initOpenTelemetryMetrics(resource, inMemoryReader);

		final ResourceGroupConfig resourceGroupConfig = ResourceGroupConfig
			.builder()
			.metrics(
				Map.of(HW_SITE_CARBON_INTENSITY_METRIC, 350D, HW_SITE_ELECTRICITY_COST_METRIC, 0.12D, HW_SITE_PUE_METRIC, 1.8D)
			)
			.attributes(Map.of(SITE_ATTRIBUTE_KEY, TestConstants.SENTRY_PARIS_SITE_VALUE))
			.build();

		ResourceGroupMetricsObserver
			.builder()
			.resourceGroupKey(TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY)
			.resourceGroupConfig(resourceGroupConfig)
			.sdkMeterProvider(sdkMeterProvider)
			.build()
			.init();

		// Trigger the observer
		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertEquals(3, metrics.size());

		final Map<String, MetricData> metricMap = metrics
			.stream()
			.collect(Collectors.toMap(MetricData::getName, Function.identity()));

		final MetricData carbonIntensity = metricMap.get(HW_SITE_CARBON_INTENSITY_METRIC);
		assertNotNull(carbonIntensity);
		assertEquals("g", carbonIntensity.getUnit());
		final DoublePointData carbonIntensityDataPoint = carbonIntensity
			.getDoubleGaugeData()
			.getPoints()
			.stream()
			.findAny()
			.orElseThrow();
		assertEquals(350D, carbonIntensityDataPoint.getValue());
		assertEquals(
			TestConstants.SENTRY_PARIS_SITE_VALUE,
			carbonIntensityDataPoint.getAttributes().get(SITE_OTEL_ATTRIBUTE_KEY)
		);
		final MetricData electricityCost = metricMap.get(HW_SITE_ELECTRICITY_COST_METRIC);
		assertNotNull(electricityCost);
		assertEquals("", electricityCost.getUnit());
		final DoublePointData electricityCostDataPoint = electricityCost
			.getDoubleGaugeData()
			.getPoints()
			.stream()
			.findAny()
			.orElseThrow();
		assertEquals(0.12D, electricityCostDataPoint.getValue());
		assertEquals(
			TestConstants.SENTRY_PARIS_SITE_VALUE,
			electricityCostDataPoint.getAttributes().get(SITE_OTEL_ATTRIBUTE_KEY)
		);
		final MetricData pue = metricMap.get(HW_SITE_PUE_METRIC);
		assertEquals("1", pue.getUnit());
		assertNotNull(pue);
		final DoublePointData pueDataPoint = pue.getDoubleGaugeData().getPoints().stream().findAny().orElseThrow();
		assertEquals(1.8D, pueDataPoint.getValue());
		assertEquals(TestConstants.SENTRY_PARIS_SITE_VALUE, pueDataPoint.getAttributes().get(SITE_OTEL_ATTRIBUTE_KEY));
	}
}
