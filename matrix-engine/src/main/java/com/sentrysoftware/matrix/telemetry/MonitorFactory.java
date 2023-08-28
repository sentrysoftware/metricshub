package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.HostLocation;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.telemetry.metric.StateSetMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOSTNAME_VALUE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_CREATION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.METRIC_ATTRIBUTES_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OS_TYPE;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MonitorFactory {

	private Map<String, AbstractMetric> metrics;

	private Map<String, String> attributes;

	private Resource resource;

	private Map<String, List<AlertRule>> alertRules;

	private TelemetryManager telemetryManager;

	/**
	 * This method creates or updates the monitor
	 *
	 * @param attributes  monitor attributes
	 * @param resource    monitor resource
	 * @param monitorType the type of the monitor
	 * @return Monitor instance
	 */
	public Monitor createOrUpdateMonitor(final Map<String, String> attributes, final Resource resource, final String monitorType) {
		final Monitor foundMonitor = telemetryManager.findMonitorByTypeAndId(monitorType,
				attributes.get(MONITOR_ATTRIBUTE_ID));
		if (foundMonitor != null) {
			foundMonitor.setAttributes(attributes);
			foundMonitor.setResource(resource);
			foundMonitor.setType(monitorType);
			return foundMonitor;
		} else {
			final Monitor newMonitor = Monitor
				.builder()
				.resource(resource)
				.attributes(attributes)
				.type(monitorType)
				.build();
			Map<String, Monitor> monitorsMap = telemetryManager.getMonitors().get(monitorType);
			if (monitorsMap == null) {
				monitorsMap = new HashMap<>();
			}
			monitorsMap.put(newMonitor.getAttributes().get(MONITOR_ATTRIBUTE_ID), newMonitor);
			telemetryManager.getMonitors().put(monitorType, monitorsMap);
			return newMonitor;
		}
	}

	/**
	 * This method sets a stateSet metric in the monitor
	 *
	 * @param monitor     a given monitor
	 * @param metricName  the metric's name
	 * @param value       the metric's value
	 * @param stateSet    array of states values. E.g. [ "ok", "degraded", "failed" ]
	 * @param collectTime the metric's collect time
	 */
	public void collectStateSetMetric(
		final Monitor monitor,
		final String metricName,
		final String value,
		final String[] stateSet,
		final long collectTime
	) {

		final StateSetMetric metric = monitor.getMetric(metricName, StateSetMetric.class);
		if (metric == null) {
			// Add the metric directly in the monitor's metrics
			monitor.addMetric(
				metricName,
				StateSetMetric
					.builder()
					.stateSet(stateSet)
					.name(metricName)
					.collectTime(collectTime)
					.value(value)
					.attributes(extractAttributesFromMetricName(metricName))
					.build()
			);
		} else {
			// stateSet, metricName, and metric's attributes will never change over the collects
			// so, we only set the value and collect time
			metric.setValue(value);
			metric.setCollectTime(collectTime);
		}

	}
	/**
	 * This method sets number metric in the monitor
	 *
	 * @param monitor     a given monitor
	 * @param metricName  the metric's name
	 * @param value       the metric's value
	 * @param collectTime the metric's collect time
	 */

	public void collectNumberMetric(
			final Monitor monitor,
			final String metricName,
			final Double value,
			final Long collectTime
	) {

		final NumberMetric metric = monitor.getMetric(metricName, NumberMetric.class);
		if (metric == null) {
			// Add the metric directly in the monitor's metrics
			monitor.addMetric(
					metricName,
					NumberMetric
							.builder()
							.name(metricName)
							.collectTime(collectTime)
							.value(value)
							.attributes(extractAttributesFromMetricName(metricName))
							.build()
			);
		} else {
			// stateSet, metricName, and metric's attributes will never change over the collects
			// so, we only set the value and collect time
			metric.setValue(value);
			metric.setCollectTime(collectTime);
		}

	}

	/**
	 * This method extracts the metric attributes from its name
	 *
	 * @param metricName the metric's name
	 * @return a Map with attributes names as keys and attributes values as values
	 */
	static Map<String, String> extractAttributesFromMetricName(final String metricName) {

		// Create a map to store the extracted attributes
		final Map<String, String> attributes = new HashMap<>();

		// Create a Matcher object
		final Matcher matcher = METRIC_ATTRIBUTES_PATTERN.matcher(metricName);

		if (matcher.find()) {
			final String attributeMap = matcher.group(1);

			// Split the attribute map into key-value pairs
			final String[] keyValuePairs = attributeMap.split(",");

			// Iterate through the key-value pairs
			for (String pair : keyValuePairs) {
				final String[] parts = pair.trim().split("=");
				if (parts.length == 2) {
					// Set the key-value pair and remove the double quotes from the value
					attributes.put(parts[0], parts[1].replace("\"", ""));
				}
			}
		}

		return attributes;
	}

	/**
	 * Creates the Host monitor
	 *
	 * @param isLocalhost Whether the host should be localhost or not.
	 * @return Monitor instance
	 */
	public Monitor createHostMonitor(final boolean isLocalhost) {
		// Get the host configuration
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();

		final String hostname = hostConfiguration.getHostname();

		// Create the host
		final Map<String, String> monitorAttributes = Map.of(
			MONITOR_ATTRIBUTE_ID,
			telemetryManager.getHostConfiguration().getHostId(),
			LOCATION,
			isLocalhost ? HostLocation.LOCAL.getKey() : HostLocation.REMOTE.getKey()
		);

		final DeviceKind deviceKind = hostConfiguration.getHostType();

		// The host resource os.type
		final String osType = HOST_TYPE_TO_OTEL_OS_TYPE.getOrDefault(
			deviceKind,
			deviceKind.getDisplayName().toLowerCase()
		);

		// The host resource host.type
		final String hostType = HOST_TYPE_TO_OTEL_HOST_TYPE.getOrDefault(
			deviceKind,
			deviceKind.getDisplayName().toLowerCase()
		);

		final Map<String, String> resourceAttributes = Map.of(
			HOST_ID, hostConfiguration.getHostId(),
			HOST_NAME,
			NetworkHelper.getFqdn(hostname),
			HOST_TYPE,
			hostType,
			OS_TYPE, osType,
			AGENT_HOST_NAME, AGENT_HOSTNAME_VALUE
		);
		final Resource monitorResource = Resource.builder().type(HOST).attributes(resourceAttributes).build();


		// Create the monitor using createOrUpdateMonitor
		final Monitor monitor = createOrUpdateMonitor(monitorAttributes, monitorResource, KnownMonitorType.HOST.getKey());

		log.debug(HOST_CREATION_MESSAGE, hostname, hostConfiguration.getHostId());

		return monitor;
	}

}
