package com.sentrysoftware.matrix.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.identity.Detection;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.DeviceTypeCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.HttpCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.IpmiCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.OsCommandCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProcessCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirementsCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ServiceCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.SnmpGetNextCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WbemCriterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WmiCriterion;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractConnectorProcessor {

	protected TelemetryManager telemetryManager;

	protected AbstractConnectorProcessor(@NonNull TelemetryManager telemetryManager) {
		this.telemetryManager = telemetryManager;
	}

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
			@NonNull HostConfiguration hostConfiguration) {

		final String hostname = hostConfiguration.getHostname();

		return (
			hostConfiguration.isSequential() ?
				runConnectorsSequentially(connectors, hostname) : runConnectorsSimultaneously(connectors, hostname)
		)
		.stream();

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
			connector -> connectorTestResults.add(runConnectorDetectionCriteria(connector, hostname))
		);

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
		final List<ConnectorTestResult> connectorTestResultsSynchronized = Collections.synchronizedList(connectorTestResults);

		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connectors.forEach(
			connector -> threadsPool.execute(
				() -> connectorTestResultsSynchronized.add(
					runConnectorDetectionCriteria(connector, hostname)
				)
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
		final Set<String> connectorSupersedes = connectorTestResult.getConnector().getConnectorIdentity().getDetection().getSupersedes();
		if (connectorSupersedes == null || connectorSupersedes.isEmpty()) {
			return;
		}

		supersedes.addAll(
			connectorSupersedes
				.stream()
				.map(String::toLowerCase)
				.collect(Collectors.toSet())
		);
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
		return connectorTestResults
			.stream()
			.filter(ctr ->
				monitorsSet.contains(ctr.getConnector().getConnectorIdentity().getDetection().getOnLastResort())
			)
			.toList();
	}

	/**
	 * Force the serialization when processing the given object, this method tries
	 * to acquire the connector namespace <em>lock</em> before running the
	 * executable, if the lock cannot be acquired or there is an exception or an
	 * interruption then the defaultValue is returned
	 *
	 * @param <T>          for example {@link CriterionTestResult} or a
	 *                     {@link SourceTable}
	 *
	 * @param executable   the supplier executable function, e.g. visiting a criterion or a
	 *                     source
	 * @param connector    the connector the criterion belongs to
	 * @param objToProcess the object to process used for debug purpose
	 * @param description  the object to process description used in the debug messages
	 * @param defaultValue the default value to return in case of any glitch
	 * @return T instance
	 */
	protected <T> T forceSerialization(
		@NonNull Supplier<T> executable,
		@NonNull final Connector connector,
		final Object objToProcess,
		@NonNull final String description,
		@NonNull final T defaultValue
	) {

		final ReentrantLock forceSerializationLock = getForceSerializationLock(connector);
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		final boolean isLockAcquired;
		try {
			// Try to get the lock
			isLockAcquired = forceSerializationLock.tryLock(DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Hostname {} - Interrupted exception detected when trying to acquire the force serialization lock to process {} {}. Connector: {}.",
				hostname,
				description,
				objToProcess,
				connector.getConnectorIdentity().getCompiledFilename()
			);
			log.debug(HOSTNAME_EXCEPTION_MESSAGE, hostname, e);

			Thread.currentThread().interrupt();

			return defaultValue;
		}

		if (isLockAcquired) {

			try {
				return executable.get();
			} finally {
				// Release the lock when the executable is terminated
				forceSerializationLock.unlock();
			}
		} else {
			log.error("Hostname {} - Could not acquire the force serialization lock to process {} {}. Connector: {}.",
					hostname,
					description,
					objToProcess,
					connector.getConnectorIdentity().getCompiledFilename());

			return defaultValue;
		}

	}

	/**
	 * Get the Connector Namespace force serialization lock
	 *
	 * @param connector the connector we currently process its criteria/sources/computes/
	 * @return {@link ReentrantLock} instance. never null.
	 */
	ReentrantLock getForceSerializationLock(final Connector connector) {
		return telemetryManager.getHostProperties()
			.getConnectorNamespaces()
			.get(connector.getConnectorIdentity().getCompiledFilename())
			.getForceSerializationLock();
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

		// Instantiate matsyaClientsExecutor with the telemetryManager instance
		final MatsyaClientsExecutor matsyaClientsExecutor = new MatsyaClientsExecutor(telemetryManager);

		// Instantiate criterionProcessor with matsyaClientsExecutor, telemetryManager and connector name
		final CriterionProcessor criterionProcessor = new CriterionProcessor(
			matsyaClientsExecutor,
			telemetryManager,
			connector.getConnectorIdentity().getCompiledFilename()
		);

		final Supplier<CriterionTestResult> executable;

		// Based on the type of criterion, store the call of the process method in the supplier
		if (criterion instanceof HttpCriterion httpCriterion) {
			executable = () -> criterionProcessor.process(httpCriterion);
		} else if (criterion instanceof SnmpGetCriterion snmpGetCriterion) {
			executable = () -> criterionProcessor.process(snmpGetCriterion);
		} else if (criterion instanceof SnmpGetNextCriterion snmpGetNextCriterion) {
			executable = () -> criterionProcessor.process(snmpGetNextCriterion);
		} else if (criterion instanceof OsCommandCriterion osCommandCriterion) {
			executable = () -> criterionProcessor.process(osCommandCriterion);
		} else if (criterion instanceof WmiCriterion wmiCriterion) {
			executable = () -> criterionProcessor.process(wmiCriterion);
		} else if (criterion instanceof WbemCriterion wbemCriterion) {
			executable = () -> criterionProcessor.process(wbemCriterion);
		} else if (criterion instanceof ServiceCriterion serviceCriterion) {
			executable = () -> criterionProcessor.process(serviceCriterion);
		} else if (criterion instanceof ProductRequirementsCriterion productRequirementsCriterion) {
			executable = () -> criterionProcessor.process(productRequirementsCriterion);
		} else if (criterion instanceof ProcessCriterion processCriterion) {
			executable = () -> criterionProcessor.process(processCriterion);
		} else if (criterion instanceof IpmiCriterion ipmiCriterion) {
			executable = () -> criterionProcessor.process(ipmiCriterion);
		} else if (criterion instanceof DeviceTypeCriterion deviceTypeCriterion) {
			executable = () -> criterionProcessor.process(deviceTypeCriterion);
		} else {
			throw new IllegalStateException("Unhandled criterion instance: " + criterion.getClass().getSimpleName());
		}

		// If isForceSerialization is true, call forceSerialization
		if (criterion.isForceSerialization()) {
			return forceSerialization(
				executable,
				connector,
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
	private ConnectorTestResult runConnectorDetectionCriteria(Connector connector, String hostname) {
		final Detection detection = connector.getConnectorIdentity().getDetection();

		final ConnectorTestResult connectorTestResult = ConnectorTestResult
			.builder()
			.connector(connector)
			.build();

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
