package com.sentrysoftware.matrix.agent.service.signal;

import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_BUILD_DATE_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_BUILD_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_HC_VERSION_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_NAME_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_OTEL_VERSION_NUMBER_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.AgentConstants.AGENT_INFO_VERSION_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.agent.context.AgentInfo;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import com.sentrysoftware.matrix.agent.helper.OtelTestHelper;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class SelfObserverTest {

	@Test
	void testInit() {
		final AgentInfo agentInfo = new AgentInfo();
		final Resource resource = OtelHelper.createOpenTelemetryResource(agentInfo.getResourceAttributes());
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = OtelTestHelper.initOpenTelemetryMetrics(resource, inMemoryReader);

		SelfObserver
			.builder()
			.userAttributes(Map.of(COMPANY_ATTRIBUTE_KEY, COMPANY_ATTRIBUTE_VALUE))
			.metricAttributes(agentInfo.getMetricAttributes())
			.sdkMeterProvider(sdkMeterProvider)
			.build()
			.init();

		// Trigger the observer
		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertEquals(1, metrics.size());

		final Map<String, MetricData> metricMap = metrics
			.stream()
			.collect(Collectors.toMap(MetricData::getName, Function.identity()));

		final MetricData agentInfoMetric = metricMap.get(AgentInfo.METRICS_HUB_AGENT_METRIC_NAME);
		assertNotNull(agentInfoMetric);

		final GaugeData<LongPointData> longGaugeData = agentInfoMetric.getLongGaugeData();
		final LongPointData dataPoint = longGaugeData.getPoints().stream().findAny().orElse(null);
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
