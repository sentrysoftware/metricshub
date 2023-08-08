package com.sentrysoftware.matrix.strategy.detection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.NonNull;

public abstract class AbstractConnectorProcessor {

	protected static final int MAX_THREADS_COUNT = 50;
	protected static final long THREAD_TIMEOUT = 15 * 60L; // 15 minutes

	/**
	 * Run the Detection job and returns the detected {@link ConnectorTestResult}
	 * @param telemetryManager The telemetry manager
	 * @return The {@link List} of {@link ConnectorTestResult}
	 */
	public abstract List<ConnectorTestResult> run(TelemetryManager telemetryManager);

	/**
	 * Run all detection criteria of the {@link Connector} on the {@link HostConfiguration}
	 * @param connectors
	 * @param hostConfiguration
	 * @return
	 */
	public Stream<ConnectorTestResult> runAllConnectorsDetectionCriteria(
			@NonNull Stream<Connector> connectors,
			@NonNull HostConfiguration hostConfiguration) {

		List<ConnectorTestResult> result = new ArrayList<>();
		final String hostname = hostConfiguration.getHostname();

		result = hostConfiguration.isSequential() ?
				runConnectorsSequentially(connectors, hostname) :
					runConnectorsSimultaneously(connectors, hostname);

		return result.stream();
	}

	/**
	 * Run all connectors sequentially
	 * @param connectors The connectors to run
	 * @param hostname   The name of the host
	 * @return The result of each connector
	 */
	private List<ConnectorTestResult> runConnectorsSequentially(
			@NonNull Stream<Connector> connectors,
			@NonNull String hostname) {
		final List<ConnectorTestResult> connectorTestResults = new ArrayList<>();
		connectors.forEach(
				connector -> connectorTestResults.add(runConnectorDetectionCriteria(connector, hostname)));
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
			@NonNull String hostname) {
		final List<ConnectorTestResult> connectorTestResults = new ArrayList<>();
		final List<ConnectorTestResult> connectorTestResultsSynchronized = Collections.synchronizedList(connectorTestResults);

		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connectors.forEach(connector -> threadsPool
				.execute(() -> connectorTestResultsSynchronized.add(runConnectorDetectionCriteria(connector, hostname))));

		// Order the shutdown
		threadsPool.shutdown();

		try {
			// Blocks until all tasks have completed execution after a shutdown request
			threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (Exception e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
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
		final Set<String> connectorSupersedes = connectorTestResult.getConnector().getConnectorIdentity().getDetection().getSupersedes();
		if (connectorSupersedes == null || connectorSupersedes.isEmpty()) {
			return;
		}

		supersedes.addAll(connectorSupersedes.stream().map(fileName -> fileName.toLowerCase()).collect(Collectors.toSet()));
	}

	/**
	 * Remove the {@link ConnectorTestResult} from connectorTestResults which OnLastResort monitor type is already detected
	 * by another {@link ConnectorTestResult} in connectorTestResults
	 *
	 * @param connectorTestResults The {@link List} of {@link ConnectorTestResult}
	 * @return The filtered {@link List} of {@link ConnectorTestResult}
	 */
	protected List<ConnectorTestResult> filterLastResort(@NonNull List<ConnectorTestResult> connectorTestResults) {
		final Set<String> monitorsSet = new HashSet<>();
		connectorTestResults.forEach(ctr -> monitorsSet.addAll(ctr.getConnector().getMonitors().keySet()));
		return connectorTestResults.stream()
				.filter(ctr -> monitorsSet.contains(ctr.getConnector().getConnectorIdentity().getDetection().getOnLastResort()))
				.collect(Collectors.toList());
	}

	/**
	 * Return true if the name of the {@link Connector} is in the set of connector names
	 * @param connector
	 * @param connectorNameSet
	 * @return
	 */
	protected boolean isConnectorContainedInSet(@NonNull final Connector connector, @NonNull final Set<String> connectorNameSet) {
		final ConnectorIdentity connectorIdentity = connector.getConnectorIdentity();
		if (connectorIdentity == null) {
			return false;
		}

		final String connectorName = connectorIdentity.getCompiledFilename();
		return connectorName != null && connectorNameSet.contains(connectorName);
	}

	private ConnectorTestResult runConnectorDetectionCriteria(Connector connector, String hostname) {
		// TODO
		return null;
	}
}
