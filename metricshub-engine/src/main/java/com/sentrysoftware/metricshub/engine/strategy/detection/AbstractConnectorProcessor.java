package com.sentrysoftware.metricshub.engine.strategy.detection;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOSTNAME_EXCEPTION_MESSAGE;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MAX_THREADS_COUNT;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.THREAD_TIMEOUT;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.SimpleMonitorJob;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.utils.ForceSerializationHelper;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractConnectorProcessor {

	@NonNull
	protected TelemetryManager telemetryManager;

	@NonNull
	protected MatsyaClientsExecutor matsyaClientsExecutor;

	/**
	 * Run the Detection job and returns the detected {@link ConnectorTestResult}
	 *
	 * @return The {@link List} of {@link ConnectorTestResult}
	 */
	public abstract List<ConnectorTestResult> run();

	/**
	 * Run all detection criteria of the {@link Connector} on the {@link HostConfiguration}
	 * @param connectors
	 * @param hostConfiguration
	 * @return
	 */
	public Stream<ConnectorTestResult> runAllConnectorsDetectionCriteria(
		@NonNull Stream<Connector> connectors,
		@NonNull HostConfiguration hostConfiguration
	) {
		final String hostname = hostConfiguration.getHostname();

		return (
			hostConfiguration.isSequential()
				? runConnectorsSequentially(connectors, hostname)
				: runConnectorsSimultaneously(connectors, hostname)
		).stream();
	}

	/**
	 * Run all connectors sequentially
	 * @param connectors The connectors to run
	 * @param hostname   The name of the host
	 * @return The result of each connector
	 */
	private List<ConnectorTestResult> runConnectorsSequentially(
		@NonNull Stream<Connector> connectors,
		@NonNull String hostname
	) {
		final List<ConnectorTestResult> connectorTestResults = new ArrayList<>();

		connectors.forEach(connector -> connectorTestResults.add(runConnectorDetectionCriteria(connector, hostname)));

		return connectorTestResults;
	}

	/**
	 * Run all connectors simultaneously
	 * @param connectors The connectors to run
	 * @param hostname   The name of the host
	 * @return The result of each connector
	 */
	private List<ConnectorTestResult> runConnectorsSimultaneously(
		@NonNull Stream<Connector> connectors,
		@NonNull String hostname
	) {
		final List<ConnectorTestResult> connectorTestResults = new ArrayList<>();
		final List<ConnectorTestResult> connectorTestResultsSynchronized = Collections.synchronizedList(
			connectorTestResults
		);

		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connectors.forEach(connector ->
			threadsPool.execute(() -> connectorTestResultsSynchronized.add(runConnectorDetectionCriteria(connector, hostname))
			)
		);

		// Order the shutdown
		threadsPool.shutdown();

		try {
			// Blocks until all tasks have completed execution after a shutdown request
			threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			log.error("Hostname {} - Exception encountered while running connectors simultaneously.", hostname);
			log.debug(HOSTNAME_EXCEPTION_MESSAGE, hostname, e);
		}

		return connectorTestResults;
	}

	/**
	 * Update the given {@link Set} of supersedes connectors
	 *
	 * @param supersedes
	 * @param connectorTestResult
	 */
	void updateSupersedes(@NonNull final Set<String> supersedes, @NonNull final ConnectorTestResult connectorTestResult) {
		final Set<String> connectorSupersedes = connectorTestResult
			.getConnector()
			.getConnectorIdentity()
			.getDetection()
			.getSupersedes();
		if (connectorSupersedes == null || connectorSupersedes.isEmpty()) {
			return;
		}

		supersedes.addAll(connectorSupersedes.stream().map(String::toLowerCase).collect(Collectors.toSet()));
	}

	/**
	 * Removes detected connectors of type "last resort" if their specified "last resort" monitor type (enclosure, fan, etc.) is already
	 * discovered by a "regular" connector.
	 *
	 * @param matchingConnectorList The list of detected connectors, that match the host
	 * @param hostname      		The name of the host currently discovered
	 *
	 */
	void filterLastResortConnectors(List<ConnectorTestResult> matchingConnectorList, final String hostname) {
		// Extract the list of last resort connectors from the list of matching connectors
		final List<ConnectorTestResult> lastResortConnectorList = matchingConnectorList
			.stream()
			.filter(tc -> tc.getConnector().getConnectorIdentity().getDetection().getOnLastResort() != null)
			.collect(Collectors.toList());

		if (lastResortConnectorList.isEmpty()) {
			return;
		}

		// Extract the list of regular connectors connectors from the list of matching connectors
		final List<ConnectorTestResult> regularConnectorList = matchingConnectorList
			.stream()
			.filter(tc -> tc.getConnector().getConnectorIdentity().getDetection().getOnLastResort() == null)
			.collect(Collectors.toList());

		// Go through the list of last resort connectors and remove them if a regular connector discovers the same monitor type
		String[] connectorNameHolder = new String[1];
		lastResortConnectorList.forEach(lastResortTC -> {
			boolean hasLastResortMonitor = regularConnectorList
				.stream()
				.anyMatch(tc -> {
					final Map<String, MonitorJob> monitorJobs = tc.getConnector().getMonitors();

					// Remember connector's filename
					connectorNameHolder[0] = tc.getConnector().getCompiledFilename();

					if (monitorJobs == null || monitorJobs.isEmpty()) {
						log.warn(
							"Hostname {} - {} connector detection. On last resort filter: Connector {} has no hardware monitors.",
							hostname,
							hostname,
							connectorNameHolder[0]
						);

						return false;
					}

					// The monitor's job and mapping must not be empty
					return monitorJobs
						.entrySet()
						.stream()
						.anyMatch(mj -> {
							final MonitorJob monitorJob = mj.getValue();
							final boolean hasMonitorType = lastResortTC
								.getConnector()
								.getConnectorIdentity()
								.getDetection()
								.getOnLastResort()
								.equals(mj.getKey());
							if (monitorJob != null) {
								if (monitorJob instanceof SimpleMonitorJob simpleMonitorJob) {
									return (
										hasMonitorType &&
										simpleMonitorJob.getSimple() != null &&
										simpleMonitorJob.getSimple().getMapping() != null
									);
								} else if (monitorJob instanceof StandardMonitorJob standardMonitorJob) {
									return (
										hasMonitorType &&
										standardMonitorJob.getDiscovery() != null &&
										standardMonitorJob.getDiscovery().getMapping() != null
									);
								}
							}
							return false;
						});
				});

			if (hasLastResortMonitor) {
				// The current connector discovers the same type has the defined last resort type. Discard last resort connector
				matchingConnectorList.remove(lastResortTC);

				log.info(
					"Hostname {} - {} is a \"last resort\" connector and its components are already discovered thanks to connector {}. Connector is therefore discarded.",
					hostname,
					lastResortTC.getConnector().getCompiledFilename(),
					connectorNameHolder[0]
				);
			} else {
				// Add the last resort connector to the list of "regular" connectors so that it prevents other
				// last resort connectors of the same type from matching (but that should never happen, right connector developers?)
				regularConnectorList.add(lastResortTC);
			}
		});
	}

	/**
	 * Run the criterion processor which implements the logic that needs to be
	 * executed for this criterion instance.
	 *
	 * @param criterion The criterion we wish to process
	 * @param connector The {@link Connector} defining the criterion
	 * @return <code>true</code> if the criterion execution succeeded
	 */
	protected CriterionTestResult processCriterion(final Criterion criterion, final Connector connector) {
		// Instantiate criterionProcessor with matsyaClientsExecutor, telemetryManager and connector name
		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			matsyaClientsExecutor,
			telemetryManager,
			connector.getConnectorIdentity().getCompiledFilename()
		);

		final Supplier<CriterionTestResult> executable;

		// Based on the type of criterion, store the call of the process method in the supplier
		executable = () -> criterion.accept(criterionProcessor);

		// If isForceSerialization is true, call forceSerialization
		if (criterion.isForceSerialization()) {
			return ForceSerializationHelper.forceSerialization(
				executable,
				telemetryManager,
				connector.getCompiledFilename(),
				criterion,
				"criterion",
				CriterionTestResult.empty()
			);
		} else {
			return executable.get();
		}
	}

	/**
	 * Return true if the name of the {@link Connector} is in the set of connector names
	 * @param connector
	 * @param connectorNameSet
	 * @return boolean value
	 */
	protected boolean isConnectorContainedInSet(
		@NonNull final Connector connector,
		@NonNull final Set<String> connectorNameSet
	) {
		final ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();
		if (connectorIdentity == null) {
			return false;
		}

		final String connectorName = connectorIdentity.getCompiledFilename();
		return connectorName != null && connectorNameSet.contains(connectorName);
	}

	/**
	 * Run the detection using the criteria defined in the given connector.
	 *
	 * @param connector The connector we wish to test
	 * @param hostname  The hostname of the host device
	 *
	 * @return {@link ConnectorTestResult} instance which tells if the connector test succeeded or not.
	 */
	public ConnectorTestResult runConnectorDetectionCriteria(Connector connector, String hostname) {
		final Detection detection = connector.getConnectorIdentity().getDetection();

		final ConnectorTestResult connectorTestResult = ConnectorTestResult.builder().connector(connector).build();

		if (detection == null) {
			log.warn(
				"Hostname {} - The connector {} DOES NOT match the platform as it has no detection to test.",
				hostname,
				connector.getConnectorIdentity().getCompiledFilename()
			);
			return connectorTestResult;
		}

		final List<Criterion> criteria = detection.getCriteria();

		if (criteria == null || criteria.isEmpty()) {
			log.warn(
				"Hostname {} - The connector {} DOES NOT match the platform as it has no criteria to test.",
				hostname,
				connector.getConnectorIdentity().getCompiledFilename()
			);
			return connectorTestResult;
		}

		for (final Criterion criterion : criteria) {
			final CriterionTestResult criterionTestResult = processCriterion(criterion, connector);
			if (!criterionTestResult.isSuccess()) {
				log.debug(
					"Hostname {} - Detected failed criterion for connector {}. Message: {}.",
					hostname,
					connector.getConnectorIdentity().getCompiledFilename(),
					criterionTestResult.getMessage()
				);
			}

			connectorTestResult.getCriterionTestResults().add(criterionTestResult);
		}

		return connectorTestResult;
	}
}
