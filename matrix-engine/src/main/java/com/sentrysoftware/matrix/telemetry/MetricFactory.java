package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.strategy.detection.ConnectorTestResult;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.telemetry.metric.StateSetMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EQUALS_OPERATOR;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.METRIC_ATTRIBUTES_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricFactory {

	private TelemetryManager telemetryManager;

	/**
	 * This method sets a stateSet metric in the monitor
	 *
	 * @param monitor a given monitor
	 * @param metricName the metric's name
	 * @param value the metric's value
	 * @param stateSet array of states values. E.g. [ "ok", "degraded", "failed" ]
	 * @param collectTime the metric's collect time
	 * @return collected metric
	 */
	public StateSetMetric collectStateSetMetric(
			final Monitor monitor,
			final String metricName,
			final String value,
			final String[] stateSet,
			final long collectTime
	) {

		final StateSetMetric metric = monitor.getMetric(metricName, StateSetMetric.class);
		if (metric == null) {
			// Add the metric directly in the monitor's metrics
			final StateSetMetric newMetric = StateSetMetric
				.builder()
				.stateSet(stateSet)
				.name(metricName)
				.collectTime(collectTime)
				.value(value)
				.attributes(extractAttributesFromMetricName(metricName))
				.build();
			monitor.addMetric(metricName, newMetric);
			return newMetric;
		} else {
			// stateSet, metricName, and metric's attributes will never change over the collects
			// so, we only set the value and collect time
			metric.setValue(value);
			metric.setCollectTime(collectTime);
			return metric;
		}
	}

	/**
	 * This method extracts the metric attributes from its name
	 *
	 * @param metricName the metric's name
	 * @return a Map with attributes names as keys and attributes values as values
	 */
	public static Map<String, String> extractAttributesFromMetricName(final String metricName) {

		// Create a map to store the extracted attributes
		final Map<String, String> attributes = new HashMap<>();

		// Create a Matcher object
		final Matcher matcher = METRIC_ATTRIBUTES_PATTERN.matcher(metricName);

		if (matcher.find()) {
			final String attributeMap = matcher.group(1);

			// Split the attribute map into key-value pairs
			final String[] keyValuePairs = attributeMap.split(COMMA);

			// Iterate through the key-value pairs
			for (String pair : keyValuePairs) {
				final String[] parts = pair.trim().split(EQUALS_OPERATOR);
				if (parts.length == 2) {
					// Set the key-value pair and remove the double quotes from the value
					attributes.put(parts[0], parts[1].replace("\"", EMPTY));
				}
			}
		}

		return attributes;
	}

	/**
	 * This method sets number metric in the monitor
	 *
	 * @param monitor a given monitor
	 * @param name the metric's name
	 * @param value the metric's value
	 * @param collectTime the metric's collect time
	 * @return collected metric
	 */
	public NumberMetric collectNumberMetric(
		final Monitor monitor,
		final String name,
		@NonNull final Double value,
		final Long collectTime
	) {

		final NumberMetric metric = monitor.getMetric(name, NumberMetric.class);
		if (metric == null) {
			// Add the metric directly in the monitor's metrics
			final NumberMetric newMetric = NumberMetric
				.builder()
				.name(name)
				.collectTime(collectTime)
				.value(value)
				.attributes(extractAttributesFromMetricName(name))
				.build();
			monitor.addMetric(name , newMetric);
			return newMetric;
		} else {
			// stateSet, metricName, and metric's attributes will never change over the collects
			// so, we only set the value and collect time
			metric.setValue(value);
			metric.setCollectTime(collectTime);
			return metric;
		}

	}

	/**
	 * This method sets number metric in the monitor
	 *
	 * @param monitor a given monitor
	 * @param name the metric's name
	 * @param value the metric's value
	 * @param collectTime the metric's collect time
	 * @return collected metric
	 */

	public NumberMetric collectNumberMetric(
		final Monitor monitor,
		final String name,
		@NonNull final String value,
		final Long collectTime
	) {

		try {
			return collectNumberMetric(monitor, name, Double.parseDouble(value), collectTime);
		} catch (Exception e) {
			log.warn("Hostname {} - Cannot parse the {} value '{}' for monitor id {}. {} won't be collected",
				telemetryManager.getHostConfiguration().getHostname(),
				name,
				value,
				monitor.getAttributes().get(MONITOR_ATTRIBUTE_ID),
				name
			);
			return null;
		}
	}

	/**
	 * Collect the connector status metric as a number
	 *
	 * @param connectorTestResult contains information about connector tests
	 * @param monitorFactory is responsible for metric collections
	 * @param monitor the monitor we currently collect its status metric
	 */
	public void collectConnectorStatusNumberMetric(final ConnectorTestResult connectorTestResult,
													final MonitorFactory monitorFactory, final Monitor monitor, final long strategyTime) {
		final MetricFactory metricFactory = new MetricFactory(telemetryManager);
		if (connectorTestResult.isSuccess()) {
			metricFactory.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 1.0, strategyTime);
		} else {
			metricFactory.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 0.0, strategyTime);
		}
	}

}
