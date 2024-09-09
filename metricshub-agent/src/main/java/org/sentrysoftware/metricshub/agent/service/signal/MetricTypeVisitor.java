package org.sentrysoftware.metricshub.agent.service.signal;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.sentrysoftware.metricshub.agent.config.StateSetMetricCompression;
import org.sentrysoftware.metricshub.agent.helper.OtelHelper;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricDefinition;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.Counter;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.Gauge;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.IMetricTypeVisitor;
import org.sentrysoftware.metricshub.engine.connector.model.metric.MetricType.UpDownCounter;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;

/**
 * Visitor implementation for handling different metric types and initializing appropriate observers for each.
 * The visitor initializes the metric observers for the OTEL SDK based the configured compression level for the state set metrics.
 */
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
	private String stateSetCompression;

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
			final Consumer<String> observerInitializer = StateSetMetricCompression.SUPPRESS_ZEROS.equalsIgnoreCase(
					stateSetCompression
				)
				? state ->
					GaugeSuppressZerosStateMetricObserver
						.builder()
						.withAttributes(addStateAttribute(attributes, state))
						.withDescription(metricDefinition.getDescription())
						.withUnit(metricDefinition.getUnit())
						.withMeter(getStateSetMetricMeter(metricKey, state))
						.withMetric(stateSetMetric)
						.withMetricName(metricName)
						.withState(state)
						.build()
						.init()
				: state ->
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
			for (final String state : stateSetMetric.getStateSet()) {
				observerInitializer.accept(state);
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
			final Consumer<String> observerInitializer = StateSetMetricCompression.SUPPRESS_ZEROS.equalsIgnoreCase(
					stateSetCompression
				)
				? state ->
					CounterSuppressZerosStateMetricObserver
						.builder()
						.withAttributes(addStateAttribute(attributes, state))
						.withDescription(metricDefinition.getDescription())
						.withUnit(metricDefinition.getUnit())
						.withMeter(getStateSetMetricMeter(metricKey, state))
						.withMetric(stateSetMetric)
						.withMetricName(metricName)
						.withState(state)
						.build()
						.init()
				: state ->
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
			for (final String state : stateSetMetric.getStateSet()) {
				observerInitializer.accept(state);
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
			final Consumer<String> observerInitializer = StateSetMetricCompression.SUPPRESS_ZEROS.equalsIgnoreCase(
					stateSetCompression
				)
				? state ->
					UpDownCounterSuppressZerosStateMetricObserver
						.builder()
						.withAttributes(addStateAttribute(attributes, state))
						.withDescription(metricDefinition.getDescription())
						.withUnit(metricDefinition.getUnit())
						.withMeter(getStateSetMetricMeter(metricKey, state))
						.withMetric(stateSetMetric)
						.withMetricName(metricName)
						.withState(state)
						.build()
						.init()
				: state ->
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
			for (final String state : stateSetMetric.getStateSet()) {
				observerInitializer.accept(state);
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
