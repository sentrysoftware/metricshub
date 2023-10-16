package com.sentrysoftware.matrix.agent.service.signal;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HW_METRIC;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.METRIC_DESCRIPTION;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.METRIC_INSTRUMENTATION_SCOPE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.METRIC_UNIT;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.OTEL_COMPANY_ATTRIBUTE_KEY;
import static org.junit.jupiter.api.Assertions.*;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimpleUpDownCounterMetricObserverTest {

	@Test
	void testInit() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		SimpleUpDownCounterMetricObserver
			.builder()
			.withAttributes(ATTRIBUTES)
			.withMetricName(HW_METRIC)
			.withDescription(METRIC_DESCRIPTION)
			.withUnit(METRIC_UNIT)
			.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
			.withMetricValue(1.0)
			.build()
			.init();

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(1, metricList.size());
		final MetricData hwMetricData = metricList.get(0);

		assertNotNull(hwMetricData);

		assertEquals(METRIC_DESCRIPTION, hwMetricData.getDescription());
		assertEquals(METRIC_UNIT, hwMetricData.getUnit());

		final SumData<DoublePointData> doubleData = hwMetricData.getDoubleSumData();
		final DoublePointData dataPoint = doubleData.getPoints().stream().findAny().orElse(null);
		assertNotNull(dataPoint);
		assertEquals(1, dataPoint.getValue());

		final Attributes collectedAttributes = dataPoint.getAttributes();

		assertEquals(COMPANY_ATTRIBUTE_VALUE, collectedAttributes.get(OTEL_COMPANY_ATTRIBUTE_KEY));
	}
}
