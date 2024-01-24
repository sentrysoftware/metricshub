package org.sentrysoftware.metricshub.agent.service.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.spy;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_NAME_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.AgentConstants.AGENT_INFO_VERSION_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.sentrysoftware.metricshub.agent.config.AgentConfig;
import org.sentrysoftware.metricshub.agent.context.AgentInfo;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.agent.helper.OtelHelper;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class SelfObserverSchedulingTest {

	@Test
	void testSchedule() {
		final AgentInfo agentInfo = new AgentInfo();
		final AgentConfig agentConfig = AgentConfig
			.builder()
			.attributes(Map.of(COMPANY_ATTRIBUTE_KEY, COMPANY_ATTRIBUTE_VALUE))
			.build();
		final Map<String, String> resourceAttributes = new HashMap<>();
		ConfigHelper.mergeAttributes(agentInfo.getResourceAttributes(), resourceAttributes);
		ConfigHelper.mergeAttributes(agentConfig.getAttributes(), resourceAttributes);
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();

		final ThreadPoolTaskScheduler taskSchedulerMock = spy(ThreadPoolTaskScheduler.class);
		final ScheduledFuture<?> scheduledFutureMock = spy(ScheduledFuture.class);

		doReturn(scheduledFutureMock).when(taskSchedulerMock).schedule(any(Runnable.class), any(Trigger.class));

		try (MockedStatic<OtelHelper> otelHelperMocked = mockStatic(OtelHelper.class)) {
			otelHelperMocked.when(() -> OtelHelper.createOpenTelemetryResource(anyMap())).thenCallRealMethod();
			otelHelperMocked.when(() -> OtelHelper.buildOtelAttributesFromMap(anyMap())).thenCallRealMethod();
			otelHelperMocked.when(() -> OtelHelper.isAcceptedKey(anyString())).thenCallRealMethod();

			otelHelperMocked
				.when(() -> OtelHelper.initOpenTelemetrySdk(any(Resource.class), any()))
				.thenAnswer(answer -> {
					final var sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

					sdkBuilder.addMeterProviderCustomizer((builder, u) -> {
						return builder.registerMetricReader(inMemoryReader).setResource(answer.getArgument(0));
					});

					return sdkBuilder.build();
				});

			SelfObserverScheduling
				.builder()
				.withAgentConfig(agentConfig)
				.withAgentInfo(agentInfo)
				.withOtelSdkConfiguration(new HashMap<>())
				.withSchedules(new HashMap<>())
				.withTaskScheduler(taskSchedulerMock)
				.build()
				.schedule();

			// Trigger the observer
			final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

			assertFalse(metrics.isEmpty());

			final List<MetricData> metricList = metrics
				.stream()
				.filter(metricData -> AgentInfo.METRICS_HUB_AGENT_METRIC_NAME.equals(metricData.getName()))
				.toList();

			assertEquals(1, metricList.size());

			final MetricData agentInfoMetric = metricList.get(0);
			assertNotNull(agentInfoMetric);

			final GaugeData<DoublePointData> doubleGaugeData = agentInfoMetric.getDoubleGaugeData();
			final DoublePointData dataPoint = doubleGaugeData.getPoints().stream().findAny().orElse(null);
			assertNotNull(dataPoint);
			assertEquals(1, dataPoint.getValue());

			final Attributes attributes = dataPoint.getAttributes();

			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_NAME_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_VERSION_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY)));
			assertNotNull(attributes.get(AttributeKey.stringKey(COMPANY_ATTRIBUTE_KEY)));
		}
	}
}
