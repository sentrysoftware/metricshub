package org.sentrysoftware.metricshub.agent.service.signal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.ATTRIBUTES;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HOSTNAME;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HW_METRIC;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_DESCRIPTION;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_DEGRADED;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_FAILED;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_STATE_OK;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.METRIC_UNIT;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.OTEL_COMPANY_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.metric.IMetricType;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType;
import org.sentrysoftware.metricshub.engine.connector.model.metric.StateSet;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

class MetricTypeVisitorTest {

	private static final Set<String> STATE_SET = Set.of(METRIC_STATE_OK, METRIC_STATE_DEGRADED, METRIC_STATE_FAILED);

	private static final String KAFKA_MONITOR_ID = "kafka";

	private static final AttributeKey<String> OTEL_STATE_ATTRIBUTE_KEY = AttributeKey.stringKey(
		MetricTypeVisitor.METRIC_STATE_ATTRIBUTE
	);

	@Test
	void testVisitCounter() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		MetricType.COUNTER
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition
							.builder()
							.type(MetricType.COUNTER)
							.description(METRIC_DESCRIPTION)
							.unit(METRIC_UNIT)
							.build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(NumberMetric.builder().collectTime(System.currentTimeMillis()).value(1.0).build())
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(1, metricList.size());
		assertCounterMetricData(metricList.get(0), 1);
	}

	@Test
	void testVisitStateCounter() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		final IMetricType stateSet = StateSet.builder().output(MetricType.COUNTER).set(STATE_SET).build();

		stateSet
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition.builder().type(stateSet).description(METRIC_DESCRIPTION).unit(METRIC_UNIT).build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(
						StateSetMetric
							.builder()
							.collectTime(System.currentTimeMillis())
							.stateSet(STATE_SET.toArray(new String[STATE_SET.size()]))
							.value(METRIC_STATE_OK)
							.build()
					)
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(3, metricList.size());

		// state=ok
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_OK.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 1);
		}

		// state=degraded
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_DEGRADED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 0);
		}

		// state=failed
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_FAILED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 0);
		}
	}

	@Test
	void testVisitGauge() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		MetricType.GAUGE
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition.builder().type(MetricType.GAUGE).description(METRIC_DESCRIPTION).unit(METRIC_UNIT).build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(NumberMetric.builder().collectTime(System.currentTimeMillis()).value(1.0).build())
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(1, metricList.size());
		assertGaugeMetricData(metricList.get(0), 1);
	}

	@Test
	void testVisitStateGauge() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		final IMetricType stateSet = StateSet.builder().output(MetricType.GAUGE).set(STATE_SET).build();

		stateSet
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition.builder().type(stateSet).description(METRIC_DESCRIPTION).unit(METRIC_UNIT).build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(
						StateSetMetric
							.builder()
							.collectTime(System.currentTimeMillis())
							.stateSet(STATE_SET.toArray(new String[STATE_SET.size()]))
							.value(METRIC_STATE_OK)
							.build()
					)
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(3, metricList.size());

		// state=ok
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleGaugeData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_OK.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertGaugeMetricData(metricDataList.get(0), 1);
		}

		// state=degraded
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleGaugeData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_DEGRADED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertGaugeMetricData(metricDataList.get(0), 0);
		}

		// state=failed
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleGaugeData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_FAILED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertGaugeMetricData(metricDataList.get(0), 0);
		}
	}

	@Test
	void testVisitUpDownCounter() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		MetricType.UP_DOWN_COUNTER
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition
							.builder()
							.type(MetricType.UP_DOWN_COUNTER)
							.description(METRIC_DESCRIPTION)
							.unit(METRIC_UNIT)
							.build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(NumberMetric.builder().collectTime(System.currentTimeMillis()).value(1.0).build())
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(1, metricList.size());
		assertCounterMetricData(metricList.get(0), 1);
	}

	@Test
	void testVisitStateUpDownCounter() {
		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(inMemoryReader).build();

		final IMetricType stateSet = StateSet.builder().output(MetricType.UP_DOWN_COUNTER).set(STATE_SET).build();

		stateSet
			.get()
			.getMetricKeyType()
			.accept(
				MetricTypeVisitor
					.builder()
					.withAttributes(ATTRIBUTES)
					.withMetricName(HW_METRIC)
					.withMetricDefinition(
						MetricDefinition.builder().type(stateSet).description(METRIC_DESCRIPTION).unit(METRIC_UNIT).build()
					)
					.withSdkMeterProvider(sdkMeterProvider)
					.withMetric(
						StateSetMetric
							.builder()
							.collectTime(System.currentTimeMillis())
							.stateSet(STATE_SET.toArray(new String[STATE_SET.size()]))
							.value(METRIC_STATE_OK)
							.build()
					)
					.withMonitorId(KAFKA_MONITOR_ID)
					.withResourceKey(HOSTNAME)
					.withResourceGroupKey(SENTRY_PARIS_RESOURCE_GROUP_KEY)
					.build()
			);

		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();
		assertFalse(metrics.isEmpty());

		final List<MetricData> metricList = metrics
			.stream()
			.filter(metricData -> HW_METRIC.equals(metricData.getName()))
			.toList();

		assertEquals(3, metricList.size());

		// state=ok
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_OK.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 1);
		}

		// state=degraded
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_DEGRADED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 0);
		}

		// state=failed
		{
			final List<MetricData> metricDataList = metricList
				.stream()
				.filter(metricData ->
					metricData
						.getDoubleSumData()
						.getPoints()
						.stream()
						.allMatch(doubleDataPoint ->
							METRIC_STATE_FAILED.equals(doubleDataPoint.getAttributes().get(OTEL_STATE_ATTRIBUTE_KEY))
						)
				)
				.toList();

			assertEquals(1, metricDataList.size());
			assertCounterMetricData(metricDataList.get(0), 0);
		}
	}

	/**
	 * Verify the following counter metric data
	 *
	 * @param metricData    OTEL {@link MetricData}
	 * @param expectedValue Expected value of the metric
	 */
	private void assertCounterMetricData(final MetricData metricData, int expectedValue) {
		assertNotNull(metricData);

		assertEquals(METRIC_DESCRIPTION, metricData.getDescription());
		assertEquals(METRIC_UNIT, metricData.getUnit());

		final SumData<DoublePointData> doubleData = metricData.getDoubleSumData();
		final DoublePointData dataPoint = doubleData.getPoints().stream().findAny().orElse(null);
		assertNotNull(dataPoint);
		assertEquals(expectedValue, dataPoint.getValue());

		final Attributes collectedAttributes = dataPoint.getAttributes();

		assertEquals(COMPANY_ATTRIBUTE_VALUE, collectedAttributes.get(OTEL_COMPANY_ATTRIBUTE_KEY));
	}

	/**
	 * Verify the following gauge metric data
	 *
	 * @param metricData    OTEL {@link MetricData}
	 * @param expectedValue Expected value of the metric
	 */
	private void assertGaugeMetricData(final MetricData metricData, int expectedValue) {
		assertNotNull(metricData);

		assertEquals(METRIC_DESCRIPTION, metricData.getDescription());
		assertEquals(METRIC_UNIT, metricData.getUnit());

		final GaugeData<DoublePointData> doubleData = metricData.getDoubleGaugeData();
		final DoublePointData dataPoint = doubleData.getPoints().stream().findAny().orElse(null);
		assertNotNull(dataPoint);
		assertEquals(expectedValue, dataPoint.getValue());

		final Attributes collectedAttributes = dataPoint.getAttributes();

		assertEquals(COMPANY_ATTRIBUTE_VALUE, collectedAttributes.get(OTEL_COMPANY_ATTRIBUTE_KEY));
	}
}
