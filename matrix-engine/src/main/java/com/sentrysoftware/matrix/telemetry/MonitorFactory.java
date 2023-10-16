package com.sentrysoftware.matrix.telemetry;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.UNDERSCORE;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.HostLocation;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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

	private String connectorId;

	@NonNull
	private Long discoveryTime;

	/**
	 * This method creates or updates the monitor
	 *
	 * @param id identifier of the monitor
	 * @return created or updated {@link Monitor} instance
	 */
	public Monitor createOrUpdateMonitor(final String id) {
		return createOrUpdateMonitor(attributes, resource, monitorType, id);
	}

	/**
	 * This method creates or updates the monitor
	 *
	 * @return created or updated {@link Monitor} instance
	 */
	public Monitor createOrUpdateMonitor() {
		// Build the monitor unique identifier
		final String id = buildMonitorId(connectorId, monitorType, attributes.get(MONITOR_ATTRIBUTE_ID));

		return createOrUpdateMonitor(attributes, resource, monitorType, id);
	}

	/**
	 * This method creates or updates the monitor
	 *
	 * @param attributes  monitor attributes
	 * @param resource    monitor resource
	 * @param monitorType the type of the monitor
	 * @param id          unique identifier of the monitor
	 * @return Monitor instance
	 */
	Monitor createOrUpdateMonitor(
		final Map<String, String> attributes,
		final Resource resource,
		final String monitorType,
		final String id
	) {
		return createOrUpdateMonitor(attributes, resource, monitorType, id, discoveryTime);
	}

	/**
	 * This method creates or updates the monitor
	 *
	 * @param attributes    monitor attributes
	 * @param resource      monitor resource
	 * @param monitorType   the type of the monitor
	 * @param id            unique identifier of the monitor
	 * @param discoveryTime The time of discovery
	 * @return Monitor instance
	 */
	Monitor createOrUpdateMonitor(
		final Map<String, String> attributes,
		final Resource resource,
		final String monitorType,
		final String id,
		final long discoveryTime
	) {
		final Monitor foundMonitor = telemetryManager.findMonitorByTypeAndId(monitorType, id);
		final String hostname = telemetryManager.getHostname();

		if (foundMonitor != null) {
			foundMonitor.setAttributes(attributes);
			foundMonitor.setResource(resource);
			foundMonitor.setType(monitorType);
			foundMonitor.setAsPresent(hostname);
			foundMonitor.setDiscoveryTime(discoveryTime);

			return foundMonitor;
		} else {
			final Monitor newMonitor = Monitor
				.builder()
				.resource(resource)
				.attributes(attributes)
				.type(monitorType)
				.id(id)
				.discoveryTime(discoveryTime)
				.build();

			newMonitor.setAsPresent(hostname);

			if (connectorId != null) {
				newMonitor.addAttribute(MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID, connectorId);
			}

			telemetryManager.addNewMonitor(newMonitor, monitorType, id);

			return newMonitor;
		}
	}

	/**
	 * Creates the endpoint Host monitor
	 *
	 * @param isLocalhost Whether the host should be localhost or not.
	 * @return Monitor instance
	 */
	public Monitor createEndpointHostMonitor(final boolean isLocalhost) {
		// Get the host configuration
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();

		final String hostname = hostConfiguration.getHostname();

		// Create the host
		final Map<String, String> monitorAttributes = Map.of(
			MONITOR_ATTRIBUTE_ID,
			telemetryManager.getHostConfiguration().getHostId(),
			"location",
			isLocalhost ? HostLocation.LOCAL.getKey() : HostLocation.REMOTE.getKey(),
			IS_ENDPOINT,
			"true",
			MONITOR_ATTRIBUTE_NAME,
			telemetryManager.getHostname()
		);

		final DeviceKind deviceKind = hostConfiguration.getHostType();

		// The host resource os.type
		final String osType = HOST_TYPE_TO_OTEL_OS_TYPE.getOrDefault(deviceKind, deviceKind.getDisplayName().toLowerCase());

		// The host resource host.type
		final String hostType = HOST_TYPE_TO_OTEL_HOST_TYPE.getOrDefault(
			deviceKind,
			deviceKind.getDisplayName().toLowerCase()
		);

		final Map<String, String> resourceAttributes = Map.of(
			"host.id",
			hostConfiguration.getHostId(),
			HOST_NAME,
			NetworkHelper.getFqdn(hostname),
			"host.type",
			hostType,
			"os.type",
			osType,
			"agent.host.name",
			StringHelper.getValue(() -> InetAddress.getLocalHost().getCanonicalHostName(), "unknown")
		);
		final Resource monitorResource = Resource.builder().type("host").attributes(resourceAttributes).build();

		// Create the monitor using createOrUpdateMonitor
		final Monitor monitor = createOrUpdateMonitor(
			monitorAttributes,
			monitorResource,
			KnownMonitorType.HOST.getKey(),
			telemetryManager.getHostConfiguration().getHostId()
		);

		// Flag the host as endpoint
		monitor.setAsEndpoint();

		log.debug("Hostname {} - Created endpoint host ID: {} ", hostname, hostConfiguration.getHostId());

		return monitor;
	}

	/**
	 * Build the monitor unique identifier [connectorName]_[monitorType]_[id]
	 * @param connectorName  The connector compiled file name
	 * @param monitorType    The type of the monitor.
	 * @param id             The id of the monitor we wish to build its identifier
	 * @return {@link String} value containing the key of the monitor
	 */
	public static String buildMonitorId(
		@NonNull final String connectorName,
		@NonNull final String monitorType,
		@NonNull final String id
	) {
		return new StringBuilder()
			.append(connectorName)
			.append(UNDERSCORE)
			.append(monitorType)
			.append(UNDERSCORE)
			.append(id.replaceAll("\\s*", EMPTY))
			.toString();
	}
}
