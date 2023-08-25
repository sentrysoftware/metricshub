package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.HostLocation;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.telemetry.metric.StateSetMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOSTNAME_VALUE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CLOSING_BRACKET;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EQUALS_OPERATOR;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_CREATION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OPENING_BRACKET;
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
			final Monitor newMonitor = Monitor.builder().resource(resource).attributes(attributes).metrics(new HashMap<>()).type(monitorType).build();
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
	 * @param collectTime the metric's collect time
	 */
	public void collectStateSetMetric(final Monitor monitor, final String metricName, final String value, final long collectTime) {
		StateSetMetric metric = monitor.getMetric(metricName, StateSetMetric.class);
		if (metric == null) {
			metric = new StateSetMetric();
		}
		metric.setValue(value);
		setMetricData(metric, metricName, collectTime);
		if (monitor.getMetric(metricName, StateSetMetric.class) == null) {
			monitor.getMetrics().put(metricName, metric);
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

	public void collectNumberMetric(final Monitor monitor, final String metricName, final double value, final long collectTime) {
		NumberMetric metric = monitor.getMetric(metricName, NumberMetric.class);
		if (metric == null) {
			metric = new NumberMetric();
		}
		metric.setValue(value);
		setMetricData(metric, metricName, collectTime);
		if (monitor.getMetric(metricName, NumberMetric.class) == null) {
			monitor.getMetrics().put(metricName, metric);
		}
	}

	/**
	 * This method sets AbstractMetric data
	 *
	 * @param metric      a given monitor metric
	 * @param metricName  metric's name
	 * @param collectTime metric's collect time
	 */
	private void setMetricData(final AbstractMetric metric, final String metricName, final long collectTime) {
		metric.setName(metricName);
		metric.setCollectTime(collectTime);
		final Map<String, String> metricAttributes = extractMetricAttributesFromMetricName(metricName);
		metric.setAttributes(metricAttributes);
	}

	/**
	 * This method extracts the metric attributes from its name
	 *
	 * @param metricName the metric's name
	 * @return a Map with attributes names as keys and attributes values as values
	 */
	private Map<String, String> extractMetricAttributesFromMetricName(final String metricName) {
		final Map<String, String> metricAttributes = new HashMap<>();
		String metricAttributeKeysValuesString = null;
		if (metricName.contains(OPENING_BRACKET)) {
			metricAttributeKeysValuesString = metricName.substring(metricName.indexOf(OPENING_BRACKET), metricName.lastIndexOf(CLOSING_BRACKET));
		}
		if (metricAttributeKeysValuesString != null && !metricAttributeKeysValuesString.isBlank()) {
			final String[] attributeKeyValuePairs = metricAttributeKeysValuesString.trim().split(COMMA);
			for (String keyValue : attributeKeyValuePairs) {
				final String attributeKey = keyValue.split(EQUALS_OPERATOR)[0];
				final String attributeValue = keyValue.split(EQUALS_OPERATOR)[1];
				metricAttributes.put(attributeKey, attributeValue);
			}
		}
		return metricAttributes;
	}

	/**
	 * Creates the Host.
	 *
	 * @param isLocalhost Whether the host should be localhost or not.
	 * @return Monitor instance
	 * @throws UnknownHostException If the host's hostname could not be resolved.
	 */
	public Monitor createHostMonitor(final boolean isLocalhost) throws UnknownHostException {
		final HostProperties hostProperties = telemetryManager.getHostProperties();
		hostProperties.setLocalhost(Boolean.TRUE);

		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();

		final String hostname = hostConfiguration == null ? null : hostConfiguration.getHostname();

		// Create the host
		final Map<String, String> monitorAttributes = Map.of(MONITOR_ATTRIBUTE_ID, telemetryManager.getHostConfiguration().getHostId(),
			LOCATION, isLocalhost ? HostLocation.LOCAL.getKey() : HostLocation.REMOTE.getKey());

		// The host resource os.type
		final String osType = HOST_TYPE_TO_OTEL_OS_TYPE.getOrDefault(hostConfiguration.getHostType(),
			hostConfiguration.getHostType().getDisplayName().toLowerCase());

		// The host resource host.type
		final String hostType = HOST_TYPE_TO_OTEL_HOST_TYPE.getOrDefault(hostConfiguration.getHostType(),
			hostConfiguration.getHostType().getDisplayName().toLowerCase());


		final Map<String, String> resourceAttributes = Map.of(HOST_ID, hostConfiguration.getHostId(), HOST_NAME,
			NetworkHelper.getFqdn(hostname), HOST_TYPE, hostType, OS_TYPE, osType,
			AGENT_HOST_NAME, AGENT_HOSTNAME_VALUE);
		final Resource monitorResource = Resource.builder().type(HOST).attributes(resourceAttributes).build();


		// Create the monitor using createOrUpdateMonitor
		final Monitor monitor = createOrUpdateMonitor(monitorAttributes, monitorResource, KnownMonitorType.HOST.getKey());

		log.debug(HOST_CREATION_MESSAGE, hostname, hostConfiguration.getHostId());

		return monitor;
	}

}
