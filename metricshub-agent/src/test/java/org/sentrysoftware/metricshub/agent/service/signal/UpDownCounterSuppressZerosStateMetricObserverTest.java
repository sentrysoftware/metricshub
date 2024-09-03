package org.sentrysoftware.metricshub.agent.service.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.ATTRIBUTES;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HW_METRIC;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_DESCRIPTION;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_INSTRUMENTATION_SCOPE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_DEGRADED;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_FAILED;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_OK;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_UNIT;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.OTEL_COMPANY_ATTRIBUTE_KEY;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

class UpDownCounterSuppressZerosStateMetricObserverTest {

	@Test
	void testInitStateMatches() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		UpDownCounterSuppressZerosStateMetricObserver
			.builder()
			.withAttributes(ATTRIBUTES)
			.withMetricName(HW_METRIC)
			.withDescription(METRIC_DESCRIPTION)
			.withUnit(METRIC_UNIT)
			.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
			.withMetric(StateSetMetric.builder().collectTime(System.currentTimeMillis()).value(METRIC_STATE_OK).build())
			.withState(METRIC_STATE_OK)
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

	@Test
	void testInitStateDoesntMatchMetricRecorded() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		final StateSetMetric stateSetMetric = StateSetMetric
			.builder()
			.collectTime(System.currentTimeMillis())
			.value(METRIC_STATE_OK)
			.build();
		// The previous state was degraded, indicating the metric has transitioned from degraded to ok
		stateSetMetric.setPreviousValue(METRIC_STATE_DEGRADED);

		UpDownCounterSuppressZerosStateMetricObserver
			.builder()
			.withAttributes(ATTRIBUTES)
			.withMetricName(HW_METRIC)
			.withDescription(METRIC_DESCRIPTION)
			.withUnit(METRIC_UNIT)
			.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
			.withMetric(stateSetMetric)
			.withState(METRIC_STATE_DEGRADED)
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

		assertEquals(METRIC_DESCRIPTION, hwMetricData.getDescription());
		assertEquals(METRIC_UNIT, hwMetricData.getUnit());

		assertNotNull(hwMetricData);

		final SumData<DoublePointData> doubleData = hwMetricData.getDoubleSumData();
		final DoublePointData dataPoint = doubleData.getPoints().stream().findAny().orElse(null);
		assertNotNull(dataPoint);
		assertEquals(0, dataPoint.getValue());

		final Attributes collectedAttributes = dataPoint.getAttributes();

		assertEquals(COMPANY_ATTRIBUTE_VALUE, collectedAttributes.get(OTEL_COMPANY_ATTRIBUTE_KEY));
	}

	@Test
	void testInitStateDoesntMatchMetricNotRecorded() {
		{
			final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
			final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

			final StateSetMetric stateSetMetric = StateSetMetric
				.builder()
				.collectTime(System.currentTimeMillis())
				.value(METRIC_STATE_OK)
				.build();

			UpDownCounterSuppressZerosStateMetricObserver
				.builder()
				.withAttributes(ATTRIBUTES)
				.withMetricName(HW_METRIC)
				.withDescription(METRIC_DESCRIPTION)
				.withUnit(METRIC_UNIT)
				.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
				.withMetric(stateSetMetric)
				.withState(METRIC_STATE_DEGRADED)
				.build()
				.init();

			final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
			assertTrue(metrics.isEmpty());
		}
		{
			final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
			final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

			final StateSetMetric stateSetMetric = StateSetMetric
				.builder()
				.collectTime(System.currentTimeMillis())
				.value(METRIC_STATE_OK)
				.build();
			// The previous state was failed, indicating that the metric's state has transitioned from failed to ok (not from degraded to ok)
			stateSetMetric.setPreviousValue(METRIC_STATE_FAILED);

			UpDownCounterSuppressZerosStateMetricObserver
				.builder()
				.withAttributes(ATTRIBUTES)
				.withMetricName(HW_METRIC)
				.withDescription(METRIC_DESCRIPTION)
				.withUnit(METRIC_UNIT)
				.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
				.withMetric(stateSetMetric)
				.withState(METRIC_STATE_DEGRADED)
				.build()
				.init();

			final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
			assertTrue(metrics.isEmpty());
		}
	}

	@Test
	void testInitStateDoesntMatchUnchangedStateMetricNotRecorded() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		final StateSetMetric stateSetMetric = StateSetMetric
			.builder()
			.collectTime(System.currentTimeMillis())
			.value(METRIC_STATE_OK)
			.build();
		// The previous state was ok, indicating that the metric's state has remained unchanged
		stateSetMetric.setPreviousValue(METRIC_STATE_OK);

		UpDownCounterSuppressZerosStateMetricObserver
			.builder()
			.withAttributes(ATTRIBUTES)
			.withMetricName(HW_METRIC)
			.withDescription(METRIC_DESCRIPTION)
			.withUnit(METRIC_UNIT)
			.withMeter(sdkMeterProvider.get(METRIC_INSTRUMENTATION_SCOPE))
			.withMetric(stateSetMetric)
			.withState(METRIC_STATE_DEGRADED)
			.build()
			.init();

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertTrue(metrics.isEmpty());
	}
}
