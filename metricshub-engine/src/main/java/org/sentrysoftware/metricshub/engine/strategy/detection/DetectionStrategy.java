package org.sentrysoftware.metricshub.engine.strategy.detection;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_APPLIES_TO_OS;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_ID;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_PARENT_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.common.helpers.NetworkHelper;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.OsCommandCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.strategy.detection.ConnectorStagingManager.StagedConnectorIdentifiers;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.MonitorFactory;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The {@code DetectionStrategy} class represents the strategy for detecting and creating monitors based on the configuration and connector detection results.
 * It is responsible for running connector detection, creating monitors for the detected connectors, and updating the status of configured connectors.
 *
 * <p>
 * The class extends {@link AbstractStrategy} and implements the detection and creation logic for monitors based on connector detection results.
 * It uses the TelemetryManager to manage monitors and metrics associated with connectors.
 * </p>
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class DetectionStrategy extends AbstractStrategy {

	/**
	 * Builder for constructing instances of {@code DetectionStrategy}.
	 *
	 * @param telemetryManager The telemetry manager for managing monitors and metrics.
	 * @param strategyTime     The time at which the detection strategy is executed.
	 * @param clientsExecutor  The executor for running connector clients.
	 */
	@Builder
	public DetectionStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, strategyTime, clientsExecutor);
	}

	@Override
	public void run() {
		final HostConfiguration hostConfiguration = telemetryManager.getHostConfiguration();
		final HostProperties hostProperties = telemetryManager.getHostProperties();
		if (hostConfiguration == null) {
			return;
		}

		final String hostname = hostConfiguration.getHostname();
		log.debug("Hostname {} - Start detection strategy.", hostname);

		// Detect if we monitor localhost then set the localhost property in the HostProperties instance
		hostProperties.setLocalhost(NetworkHelper.isLocalhost(hostname));

		// Get the configured connector
		final String configuredConnectorId = hostConfiguration.getConfiguredConnectorId();

		// Initialize a new manager to stage connectors
		final ConnectorStagingManager connectorStagingManager = new ConnectorStagingManager(hostname);

		// Stage connector identifiers
		final StagedConnectorIdentifiers stagedConnectorIdentifiers = connectorStagingManager.stage(
			telemetryManager.getConnectorStore(),
			hostConfiguration.getConnectors()
		);

		// Initialize the connector test results
		final List<ConnectorTestResult> connectorTestResults = new ArrayList<>();

		// Process forced connectors
		if (stagedConnectorIdentifiers.isForcedStaging()) {
			connectorTestResults.addAll(
				new ConnectorSelection(telemetryManager, clientsExecutor, stagedConnectorIdentifiers.getForcedConnectorIds())
					.run()
			);
		}

		// Process automatic detection if connectors are staged for automatic detection.
		// If a custom connector has been created then the automatic detection is skipped.
		if (stagedConnectorIdentifiers.isAutoDetectionStaged() && configuredConnectorId == null) {
			connectorTestResults.addAll(
				new AutomaticDetection(
					telemetryManager,
					clientsExecutor,
					stagedConnectorIdentifiers.getAutoDetectionConnectorIds()
				)
					.run()
			);
		}

		// Create Host monitor
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.discoveryTime(strategyTime)
			.build();
		monitorFactory.createEndpointHostMonitor(hostProperties.isLocalhost());

		// Create monitors
		createMonitors(connectorTestResults);

		// Create configured connector monitor
		createConfiguredConnectorMonitor(configuredConnectorId);
	}

	/**
	 * Create a new connector monitor for the configured connector
	 *
	 * @param configuredConnectorId unique identifier of the connector
	 */
	void createConfiguredConnectorMonitor(final String configuredConnectorId) {
		if (configuredConnectorId == null) {
			return;
		}

		final String hostId = telemetryManager.getHostConfiguration().getHostId();

		// Set monitor attributes
		final Map<String, String> monitorAttributes = new HashMap<>();
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, configuredConnectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, configuredConnectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_PARENT_ID, hostId);

		// Create the monitor factory
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.CONNECTOR.getKey())
			.attributes(monitorAttributes)
			.connectorId(configuredConnectorId)
			.discoveryTime(strategyTime)
			.build();

		// Create or update the monitor by calling monitor factory
		final Monitor monitor = monitorFactory.createOrUpdateMonitor(
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), configuredConnectorId)
		);

		telemetryManager.getHostProperties().getConnectorNamespace(configuredConnectorId).setStatusOk(true);

		new MetricFactory(telemetryManager.getHostname())
			.collectNumberMetric(monitor, CONNECTOR_STATUS_METRIC_KEY, 1.0, strategyTime);
	}

	/**
	 * This method creates monitors in TelemetryManager given a list of ConnectorTestResult
	 *
	 * @param connectorTestResultList List of ConnectorTestResult
	 */
	void createMonitors(final List<ConnectorTestResult> connectorTestResultList) {
		// Verify SSH for each connector
		connectorTestResultList.stream().map(ConnectorTestResult::getConnector).forEach(this::verifySsh);

		// Create a monitor for each connector
		connectorTestResultList.forEach(this::createMonitor);
	}

	/**
	 * This method creates a monitor in TelemetryManager for a given ConnectorTestResult instance
	 *
	 * @param connectorTestResult ConnectorTestResult instance
	 */
	public void createMonitor(final ConnectorTestResult connectorTestResult) {
		// Get the connector
		final Connector connector = connectorTestResult.getConnector();

		// Set monitor attributes
		final Map<String, String> monitorAttributes = new HashMap<>();
		final String hostId = telemetryManager.getHostConfiguration().getHostId();
		final String connectorId = connector.getCompiledFilename();
		monitorAttributes.put(MONITOR_ATTRIBUTE_ID, connectorId);
		monitorAttributes.put(MONITOR_ATTRIBUTE_NAME, connectorId);
		monitorAttributes.put(
			MONITOR_ATTRIBUTE_APPLIES_TO_OS,
			connector
				.getConnectorIdentity()
				.getDetection()
				.getAppliesTo()
				.stream()
				.map(deviceKind -> deviceKind.toString().toLowerCase())
				.sorted()
				.collect(Collectors.joining(MetricsHubConstants.COMMA))
		);
		monitorAttributes.put("description", connector.getConnectorIdentity().getInformation());
		monitorAttributes.put(MONITOR_ATTRIBUTE_PARENT_ID, hostId);

		// Create the monitor factory
		final MonitorFactory monitorFactory = MonitorFactory
			.builder()
			.telemetryManager(telemetryManager)
			.monitorType(KnownMonitorType.CONNECTOR.getKey())
			.attributes(monitorAttributes)
			.connectorId(connectorId)
			.discoveryTime(strategyTime)
			.build();

		// Create or update the monitor by calling monitor factory
		final Monitor monitor = monitorFactory.createOrUpdateMonitor(
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), connectorId)
		);

		collectConnectorStatus(connectorTestResult, connector, connectorId, monitor);
	}

	/**
	 * Verify the given set of sources instances to check if they are OsCommandSources
	 *
	 * @param sourceTypes Connector source types
	 */
	void verifySshSources(final Set<Class<? extends Source>> sourceTypes) {
		if (sourceTypes.contains(OsCommandSource.class)) {
			telemetryManager.getHostProperties().setMustCheckSshStatus(true);
		}
	}

	/**
	 * Verify the given list of criterion instances to check if they will run locally or remotely
	 *
	 * @param criteria Connector detection criteria list
	 */
	void verifySshCriteria(final List<Criterion> criteria) {
		boolean osCommandExecuteLocally = false;
		boolean osCommandExecuteRemotely = false;

		for (final Criterion criterion : criteria) {
			if (criterion instanceof OsCommandCriterion) {
				boolean executeLocally = ((OsCommandCriterion) criterion).getExecuteLocally();

				// if osCommandExecuteLocally is false, it will take the executeLocally value
				osCommandExecuteLocally |= executeLocally;

				// if osCommandExecuteRemotely is false, it will take the executeLocally's opposite value
				osCommandExecuteLocally |= !executeLocally;
			}

			// Stop if both variables are true
			if (osCommandExecuteLocally && osCommandExecuteRemotely) {
				break;
			}
		}

		// Store the values in the Host Properties
		telemetryManager.getHostProperties().setOsCommandExecutesLocally(osCommandExecuteLocally);
		telemetryManager.getHostProperties().setOsCommandExecutesRemotely(osCommandExecuteRemotely);
	}

	/**
	 * Verify SSH on the given connector so that the {@code metricshub.host.up}
	 * metric collect can properly assess whether commands are working or not.
	 *
	 * @param connector {@link Connector} instance defining sources and criteria
	 */
	void verifySsh(final Connector connector) {
		// Verify SSH Sources
		verifySshSources(connector.getSourceTypes());

		// Get the connector identity
		ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();

		// Test if there are connector detection criteria
		if (
			connectorIdentity != null &&
			connectorIdentity.getDetection() != null &&
			connectorIdentity.getDetection().getCriteria() != null
		) {
			// Verify SSH Criteria
			verifySshCriteria(connectorIdentity.getDetection().getCriteria());
		}
	}
}
