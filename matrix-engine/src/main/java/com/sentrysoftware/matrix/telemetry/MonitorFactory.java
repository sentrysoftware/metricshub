package com.sentrysoftware.matrix.telemetry;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_HOST_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_TYPE_TO_OTEL_OS_TYPE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.UNDERSCORE;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.alert.AlertRule;
import com.sentrysoftware.matrix.common.HostLocation;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NetworkHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;

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

	/**
	 * This method creates or updates the monitor
	 * 
	 * @param unique identifier of the monitor
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
		final String id =  buildMonitorId(
			connectorId,
			monitorType,
			telemetryManager.getHostConfiguration().getHostId(),
			attributes.get(MONITOR_ATTRIBUTE_ID)
		);

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

		final Monitor foundMonitor = telemetryManager.findMonitorByTypeAndId(monitorType, id);

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
				.id(id)
				.build();

			telemetryManager.addNewMonitor(newMonitor, monitorType, id);

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
			"location",
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
			"host.id", hostConfiguration.getHostId(),
			HOST_NAME,
			NetworkHelper.getFqdn(hostname),
			"host.type",
			hostType,
			"os.type", osType,
			"agent.host.name", StringHelper.getValue(() -> InetAddress.getLocalHost().getCanonicalHostName(), "unknown")
		);
		final Resource monitorResource = Resource.builder().type("host").attributes(resourceAttributes).build();


		// Create the monitor using createOrUpdateMonitor
		final Monitor monitor = createOrUpdateMonitor(
			monitorAttributes,
			monitorResource,
			KnownMonitorType.HOST.getKey(),
			telemetryManager.getHostConfiguration().getHostId()
		);

		log.debug("Hostname {} - Created host ID: {} ", hostname, hostConfiguration.getHostId());

		return monitor;
	}

	/**
	 * Build the monitor unique identifier [connectorName]_[monitorType]_[hostId]_[id]
	 * @param connectorName  The connector compiled file name
	 * @param monitorType    The type of the monitor. See {@link MonitorType}
	 * @param hostId       The unique identifier of the main monitor called host
	 * @param id             The id of the monitor we wish to build its identifier
	 * @return {@link String} value containing the key of the monitor
	 */
	public static String buildMonitorId(
		@NonNull final String connectorName,
		@NonNull final String monitorType,
		@NonNull final String hostId,
		@NonNull final String id
	) {
		return new StringBuilder()
			.append(connectorName)
			.append(UNDERSCORE)
			.append(monitorType)
			.append(UNDERSCORE)
			.append(hostId)
			.append(UNDERSCORE)
			.append(id.replaceAll("\\s*", EMPTY))
			.toString();
	}

}