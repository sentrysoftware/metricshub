package com.sentrysoftware.metricshub.agent.service.signal;

import com.sentrysoftware.metricshub.agent.helper.OtelHelper;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.Counter;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.Gauge;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.IMetricTypeVisitor;
import com.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.UpDownCounter;
import com.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class MetricTypeVisitor implements IMetricTypeVisitor {

	protected static final String METRIC_STATE_ATTRIBUTE = "state";

	private SdkMeterProvider sdkMeterProvider;
	private MetricDefinition metricDefinition;
	private AbstractMetric metric;
	private String monitorId;
	private String resourceKey;
	private String resourceGroupKey;
	private String metricName;
	private Attributes attributes;

	@Override
	public void visit(Gauge gauge) {
		final String metricKey = metric.getName();

		if (metric instanceof NumberMetric numberMetric) {
			GaugeMetricObserver
				.builder()
				.withAttributes(attributes)
				.withDescription(metricDefinition.getDescription())
				.withUnit(metricDefinition.getUnit())
				.withMeter(getNumberMetricMeter(metricKey))
				.withMetric(numberMetric)
				.withMetricName(metricName)
				.build()
				.init();
		} else if (metric instanceof StateSetMetric stateSetMetric) {
			for (final String state : stateSetMetric.getStateSet()) {
				GaugeStateMetricObserver
					.builder()
					.withAttributes(addStateAttribute(attributes, state))
					.withDescription(metricDefinition.getDescription())
					.withUnit(metricDefinition.getUnit())
					.withMeter(getStateSetMetricMeter(metricKey, state))
					.withMetric(stateSetMetric)
					.withMetricName(metricName)
					.withState(state)
					.build()
					.init();
			}
		}
	}

	/**
	 * Get Meter for a {@link NumberMetric}
	 *
	 * @param metricKey unique key of the metric
	 * @return OTEL SDK {@link Meter} instance
	 */
	private Meter getNumberMetricMeter(final String metricKey) {
		return sdkMeterProvider.get(String.format("%s.%s.%s.%s", resourceGroupKey, resourceKey, monitorId, metricKey));
	}

	/**
	 * Get Meter for a {@link StateSetMetric}
	 * @param metricKey unique key of the metric
	 * @param state     state of the metric
	 *
	 * @return OTEL SDK {@link Meter} instance
	 */
	private Meter getStateSetMetricMeter(final String metricKey, final String state) {
		return sdkMeterProvider.get(
			String.format("%s.%s.%s.%s.%s", resourceGroupKey, resourceKey, monitorId, metricKey, state)
		);
	}

	@Override
	public void visit(Counter counter) {
		final String metricKey = metric.getName();

		if (metric instanceof NumberMetric numberMetric) {
			CounterMetricObserver
				.builder()
				.withAttributes(attributes)
				.withDescription(metricDefinition.getDescription())
				.withUnit(metricDefinition.getUnit())
				.withMeter(getNumberMetricMeter(metricKey))
				.withMetric(numberMetric)
				.withMetricName(metricName)
				.build()
				.init();
		} else if (metric instanceof StateSetMetric stateSetMetric) {
			for (final String state : stateSetMetric.getStateSet()) {
				CounterStateMetricObserver
					.builder()
					.withAttributes(addStateAttribute(attributes, state))
					.withDescription(metricDefinition.getDescription())
					.withUnit(metricDefinition.getUnit())
					.withMeter(getStateSetMetricMeter(metricKey, state))
					.withMetric(stateSetMetric)
					.withMetricName(metricName)
					.withState(state)
					.build()
					.init();
			}
		}
	}

	@Override
	public void visit(UpDownCounter upDownCounter) {
		final String metricKey = metric.getName();

		if (metric instanceof NumberMetric numberMetric) {
			UpDownCounterMetricObserver
				.builder()
				.withAttributes(attributes)
				.withDescription(metricDefinition.getDescription())
				.withUnit(metricDefinition.getUnit())
				.withMeter(getNumberMetricMeter(metricKey))
				.withMetric(numberMetric)
				.withMetricName(metricName)
				.build()
				.init();
		} else if (metric instanceof StateSetMetric stateSetMetric) {
			for (final String state : stateSetMetric.getStateSet()) {
				UpDownCounterStateMetricObserver
					.builder()
					.withAttributes(addStateAttribute(attributes, state))
					.withDescription(metricDefinition.getDescription())
					.withUnit(metricDefinition.getUnit())
					.withMeter(getStateSetMetricMeter(metricKey, state))
					.withMetric(stateSetMetric)
					.withMetricName(metricName)
					.withState(state)
					.build()
					.init();
			}
		}
	}

	/**
	 * Add the state attribute
	 *
	 * @param attributes OTEL attributes
	 * @param stateValue the state value. E.g. ok, degraded or failed
	 * @return new {@link Attributes} instance
	 */
	Attributes addStateAttribute(final Attributes attributes, final String stateValue) {
		return OtelHelper.mergeOtelAttributes(
			attributes,
			Attributes.of(AttributeKey.stringKey(METRIC_STATE_ATTRIBUTE), stateValue)
		);
	}
}
