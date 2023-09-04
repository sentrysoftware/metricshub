package com.sentrysoftware.matrix.telemetry;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.HostLocation;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOSTNAME_VALUE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.AGENT_HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_CREATION_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOCATION;
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

	private String monitorType;

	/**
	 * This method creates or updates the monitor
	 */
	public Monitor createOrUpdateMonitor() {
		return createOrUpdateMonitor(attributes, resource, monitorType);
	}
	/**
	 * This method creates or updates the monitor
	 *
	 * @param attributes  monitor attributes
	 * @param resource    monitor resource
	 * @param monitorType the type of the monitor
	 * @return Monitor instance
	 */
	Monitor createOrUpdateMonitor(final Map<String, String> attributes, final Resource resource, final String monitorType) {
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
			if (monitorsMap != null) {
				monitorsMap.put(newMonitor.getAttributes().get(MONITOR_ATTRIBUTE_ID), newMonitor);
			} else {
				monitorsMap = new HashMap<>();
				monitorsMap.put(newMonitor.getAttributes().get(MONITOR_ATTRIBUTE_ID), newMonitor);
				telemetryManager.getMonitors().put(monitorType, monitorsMap);
			}

			return newMonitor;
		}
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
			isLocalhost ? HostLocation.LOCAL.getKey() : HostLocation.REMOTE.getKey(),
			IS_ENDPOINT,
			"true"
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