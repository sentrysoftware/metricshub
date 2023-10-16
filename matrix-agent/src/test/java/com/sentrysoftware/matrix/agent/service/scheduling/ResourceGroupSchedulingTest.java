package com.sentrysoftware.matrix.agent.service.scheduling;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SITE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.service.scheduling.ResourceGroupScheduling.HW_SITE_CARBON_INTENSITY_METRIC;
import static com.sentrysoftware.matrix.agent.service.scheduling.ResourceGroupScheduling.HW_SITE_ELECTRICITY_COST_METRIC;
import static com.sentrysoftware.matrix.agent.service.scheduling.ResourceGroupScheduling.HW_SITE_PUE_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.agent.helper.TestConstants;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class ResourceGroupSchedulingTest {

	private static final AttributeKey<String> SITE_OTEL_ATTRIBUTE_KEY = AttributeKey.stringKey(SITE_ATTRIBUTE_KEY);

	@Test
	void testSchedule() {
		final AgentConfig agentConfig = AgentConfig
			.builder()
			.attributes(Map.of(COMPANY_ATTRIBUTE_KEY, COMPANY_ATTRIBUTE_VALUE))
			.build();

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();

		final ResourceGroupConfig resourceGroupConfig = ResourceGroupConfig
			.builder()
			.metrics(
				Map.of(HW_SITE_CARBON_INTENSITY_METRIC, 350D, HW_SITE_ELECTRICITY_COST_METRIC, 0.12D, HW_SITE_PUE_METRIC, 1.8D)
			)
			.attributes(Map.of(SITE_ATTRIBUTE_KEY, TestConstants.SENTRY_PARIS_SITE_VALUE))
			.build();

		final ThreadPoolTaskScheduler taskSchedulerMock = spy(ThreadPoolTaskScheduler.class);
		final ScheduledFuture<?> scheduledFutureMock = spy(ScheduledFuture.class);

		doReturn(scheduledFutureMock).when(taskSchedulerMock).schedule(any(Runnable.class), any(Trigger.class));

		try (MockedStatic<OtelHelper> otelHelperMocked = mockStatic(OtelHelper.class)) {
			otelHelperMocked.when(() -> OtelHelper.createOpenTelemetryResource(anyMap())).thenCallRealMethod();
			otelHelperMocked.when(() -> OtelHelper.buildOtelAttributesFromMap(anyMap())).thenCallRealMethod();
			otelHelperMocked.when(() -> OtelHelper.isAcceptedKey(anyString())).thenCallRealMethod();
			otelHelperMocked
				.when(() -> OtelHelper.mergeOtelAttributes(any(Attributes.class), any(Attributes.class)))
				.thenCallRealMethod();

			otelHelperMocked
				.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any()))
				.thenAnswer(answer -> {
					final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

					sdkBuilder.addMeterProviderCustomizer((builder, u) -> {
						return builder.registerMetricReader(inMemoryReader).setResource(answer.getArgument(0));
					});

					return sdkBuilder.build();
				});

			ResourceGroupScheduling
				.builder()
				.withAgentConfig(agentConfig)
				.withOtelSdkConfiguration(new HashMap<>())
				.withResourceGroupConfig(resourceGroupConfig)
				.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
				.withSchedules(new HashMap<>())
				.withTaskScheduler(taskSchedulerMock)
				.build()
				.schedule();

			// Trigger the observer
			final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

			assertFalse(metrics.isEmpty());

			final Map<String, MetricData> metricMap = metrics
				.stream()
				.filter(metricData ->
					HW_SITE_CARBON_INTENSITY_METRIC.equals(metricData.getName()) ||
					HW_SITE_ELECTRICITY_COST_METRIC.equals(metricData.getName()) ||
					HW_SITE_PUE_METRIC.equals(metricData.getName())
				)
				.collect(Collectors.toMap(MetricData::getName, Function.identity()));

			assertEquals(3, metricMap.size());

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
}
