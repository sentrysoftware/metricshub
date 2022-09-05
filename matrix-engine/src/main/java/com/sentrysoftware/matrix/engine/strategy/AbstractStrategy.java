package com.sentrysoftware.matrix.engine.strategy;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.N_A;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static org.springframework.util.Assert.state;

import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.helpers.TextTableHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionVisitor;
import com.sentrysoftware.matrix.engine.strategy.detection.TestedConnector;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceUpdaterVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.compute.ComputeUpdaterVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.compute.ComputeVisitor;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStrategy implements IStrategy {

	@Autowired
	protected ConnectorStore store;

	@Autowired
	protected StrategyConfig strategyConfig;

	@Autowired
	protected MatsyaClientsExecutor matsyaClientsExecutor;

	@Autowired
	@Setter
	protected Long strategyTime;

	@Autowired
	protected WqlDetectionHelper wqlDetectionHelper;

	protected static final int MAX_THREADS_COUNT = 50;
	protected static final long THREAD_TIMEOUT = 15 * 60L; // 15 minutes
	public static final int DEFAULT_LOCK_TIMEOUT = 2 * 60; // 2 minutes
	private static final String LOG_COMPUTE_KEY_SUFFIX_TEMPLATE = "%s.compute(%d)";
	private static final String COMPUTE = "compute";
	private static final String SOURCE = "source";

	@Override
	public void prepare() {

	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 *
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The host's name only used for logging
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname) {

		processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname, null);
	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 *
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The host's name only used for logging
	 * @param monitor        The monitor used in the mono instance processing
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname, final Monitor monitor) {

		if (sources == null || sources.isEmpty()) {
			log.debug("Hostname {} - No sources found from connector {} with monitor {}.", hostname, connector.getCompiledFilename(), monitorType);
			return;
		}

		// Loop over all the sources and accept the SourceVisitor which is going to
		// visit and process the source
		for (final Source source : sources) {

			final String sourceKey = source.getKey();

			logBeginOperation(SOURCE, source, sourceKey, connector.getCompiledFilename(), hostname);

			final ISourceVisitor sourceVisitor = new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector);
			final SourceTable sourceTable;

			final Supplier<SourceTable> executable = () ->
				source.accept(new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig));

			if (source.isForceSerialization()) {
				sourceTable = forceSerialization(executable, connector, source, SOURCE, SourceTable.empty());
			} else {
				sourceTable = executable.get();
			}

			if (sourceTable == null) {
				log.warn("Hostname {} - Received null source table for Source key {} - Connector {} - Monitor {}.",
						hostname,
						sourceKey,
						connector.getCompiledFilename(),
						monitorType);
				continue;
			}

			// log the source table
			logSourceTable(SOURCE, source.getClass().getSimpleName(),
					sourceKey, connector.getCompiledFilename(), sourceTable, hostname);

			final List<Compute> computes = source.getComputes();

			if (computes != null) {

				final ComputeVisitor computeVisitor = new ComputeVisitor(
					sourceKey,
					sourceTable,
					connector,
					hostname,
					matsyaClientsExecutor
				);

				final ComputeUpdaterVisitor computeUpdaterVisitor = new ComputeUpdaterVisitor(
					computeVisitor,
					monitor,
					connector,
					strategyConfig
				);

				for (final Compute compute : computes) {

					// Example: enclosure.discovery.source(1).compute(1)
					final String computeKey = String.format(LOG_COMPUTE_KEY_SUFFIX_TEMPLATE, sourceKey,
							compute.getIndex());

					logBeginOperation(COMPUTE, compute, computeKey, connector.getCompiledFilename(), hostname);

					compute.accept(computeUpdaterVisitor);

					// log the updated source table
					logSourceTable(COMPUTE, compute.getClass().getSimpleName(), computeKey,
							connector.getCompiledFilename(), computeVisitor.getSourceTable(), hostname);
				}

				hostMonitoring
						.getConnectorNamespace(connector)
						.addSourceTable(sourceKey, computeVisitor.getSourceTable());
			} else {
				hostMonitoring
						.getConnectorNamespace(connector)
						.addSourceTable(sourceKey, sourceTable);
			}
		}
	}

	/**
	 * Log a begin entry for the given source
	 *
	 * @param <T>
	 *
	 * @param operationTag  the tag of the operation. E.g. source or compute
	 * @param execution     the source or the compute we want to log
	 * @param executionKey  the source or the compute unique key
	 * @param connectorName the connector file name
	 * @param hostname      the hostname
	 */
	private static <T> void logBeginOperation(final String operationTag, final T execution, final String executionKey,
			final String connectorName, final String hostname) {

		if (!log.isInfoEnabled()) {
			return;
		}

		log.info("Hostname {} - Begin {} [{} {}] for hardware connector [{}]:\n{}\n",
				hostname,
				operationTag,
				execution.getClass().getSimpleName(),
				executionKey,
				connectorName,
				execution.toString());
	}

	/**
	 * Log the {@link SourceTable} result.
	 *
	 * @param <T>
	 *
	 * @param operationTag   the tag of the operation. E.g. source or compute
	 * @param executionClassName the source or the compute class name we want to log
	 * @param executionKey   the key of the source or the compute we want to log
	 * @param connectorName  the compiled file name of the connector
	 * @param sourceTable    the source's result we wish to log
	 * @param hostname       the hostname of the source we wish to log
	 */
	private static void logSourceTable(final String operationTag, final String executionClassName,
			final String executionKey, final String connectorName, final SourceTable sourceTable, final String hostname) {

		if (!log.isInfoEnabled()) {
			return;
		}

		// Is there any raw data to log?
		if (sourceTable.getRawData() != null && (sourceTable.getTable() == null || sourceTable.getTable().isEmpty())) {
			log.info("Hostname {} - End of {} [{} {}] for hardware connector [{}].\nRaw result:\n{}\n",
					hostname,
					operationTag,
					executionClassName,
					executionKey,
					connectorName,
					sourceTable.getRawData());
			return;
		}

		if (sourceTable.getRawData() == null) {
			log.info("Hostname {} - End of {} [{} {}] for hardware connector [{}].\nTable result:\n{}\n",
					hostname,
					operationTag,
					executionClassName,
					executionKey,
					connectorName,
					TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable()));
			return;
		}

		log.info("Hostname {} - End of {} [{} {}] for hardware connector [{}].\nRaw result:\n{}\nTable result:\n{}\n",
				hostname,
				operationTag,
				executionClassName,
				executionKey,
				connectorName,
				sourceTable.getRawData(),
				TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable()));

	}

	/**
	 * Return <code>true</code> if the {@link List} of the {@link HardwareMonitor} instances is not null and not empty in the given
	 * {@link Connector}
	 *
	 * @param connector The connector we wish to check
	 * @param hostname  The system hostname used for debug purpose
	 * @return boolean value
	 */
	public boolean validateHardwareMonitors(final Connector connector, final String hostname, final String logMessageTemplate) {
		if (connector.getHardwareMonitors() == null || connector.getHardwareMonitors().isEmpty()) {
			log.warn(logMessageTemplate, hostname, connector.getCompiledFilename());
			return false;
		}

		return true;
	}

	/**
	 * Run the given {@link Connector}'s detection criteria
	 * and return true if all the criteria are successfully executed.
	 *
	 * @param connector	The {@link Connector} that should be tested.
	 * @param hostname	The hostname against which the {@link Connector} should be tested.
	 *
	 * @return			<code>true</code> if the connector matches the platform.
	 */
	public TestedConnector testConnector(final Connector connector, final String hostname) {

		final Detection detection = connector.getDetection();

		final TestedConnector testedConnector = TestedConnector.builder().connector(connector).build();

		if (detection == null) {
			log.warn("Hostname {} - The connector {} DOES NOT match the platform as it has no detection to test.",
					hostname, connector.getCompiledFilename());
			return testedConnector;
		}

		final List<Criterion> criteria = detection.getCriteria();

		if (criteria == null || criteria.isEmpty()) {
			log.warn("Hostname {} - The connector {} DOES NOT match the platform as it has no criteria to test.",
					hostname, connector.getCompiledFilename());
			return testedConnector;
		}

		for (Criterion criterion : criteria) {
			final CriterionTestResult critetionTestResult = processCriterion(criterion, connector);
			if (!critetionTestResult.isSuccess()) {
				log.debug("Hostname {} - Detected failed criterion for connector {}. Message: {}.",
						hostname,
						connector.getCompiledFilename(),
						critetionTestResult.getMessage());
			}

			testedConnector.getCriterionTestResults().add(critetionTestResult);
		}

		return testedConnector;
	}

	/**
	 * Accept the criterion visitor which implement the logic that needs to be
	 * executed for each criterion implementation
	 *
	 * @param criterion The criterion we wish to process
	 * @param connector The {@link Connector} defining the criterion
	 * @return <code>true</code> if the criterion execution succeeded
	 */
	protected CriterionTestResult processCriterion(final Criterion criterion, Connector connector) {

		final CriterionVisitor criterionVisitor = new CriterionVisitor(strategyConfig,
				matsyaClientsExecutor,
				wqlDetectionHelper,
				connector);

		final Supplier<CriterionTestResult> executable = () -> criterion.accept(criterionVisitor);

		if (criterion.isForceSerialization()) {
			return forceSerialization(executable,
					connector, criterion, "criterion", CriterionTestResult.empty());

		} else {

			return executable.get();

		}

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
	protected <T> T forceSerialization(@NonNull Supplier<T> executable, @NonNull final Connector connector,
			final Object objToProcess, @NonNull final String description, @NonNull final T defaultValue) {

		final ReentrantLock forceSerializationLock = getForceSerializationLock(connector);
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();

		final boolean isLockAcquired;
		try {
			// Try to get the lock
			isLockAcquired = forceSerializationLock.tryLock(DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Hostname {} - Interrupted exception detected when trying to acquire the force serialization lock to process {} {}. Connector: {}.",
					hostname,
					description,
					objToProcess,
					connector.getCompiledFilename());
			log.debug("Hostname {} - Exception: ", hostname, e);

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
					connector.getCompiledFilename());

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
		return strategyConfig
				.getHostMonitoring()
				.getConnectorNamespace(connector)
				.getForceSerializationLock();
	}

	/**
	 *
	 * @param testedConnector The {@link TestedConnector} instance we wish to check it is succeeded or not
	 * @return SUCCEEDED if the TestedConnector instance shows success = <code>true</code> otherwise FAILED
	 */
	protected static String getTestedConnectorStatus(final TestedConnector testedConnector) {
		return testedConnector.isSuccess() ? "SUCCEEDED" : "FAILED";
	}

	/**
	 * Build status and status information parameters for the given
	 * {@link TestedConnector}.
	 *
	 * @param testedConnector The {@link TestedConnector} whose status and status
	 *                        information should be created.
	 *
	 * @return Array of two elements, the first one is the {@link DiscreteParam}
	 *         status, and the second one is {@link TextParam} statusInformation
	 */
	protected IParameter[] buildConnectorStatusAndStatusInformation(final TestedConnector testedConnector) {

		final IParameter[] statusAndStatusInformation = new IParameter[2];

		boolean success = testedConnector.isSuccess();

		statusAndStatusInformation[0] = DiscreteParam
				.builder()
				.collectTime(strategyTime)
				.name(STATUS_PARAMETER)
				.state(success ? Status.OK : Status.FAILED)
				.build();

		statusAndStatusInformation[1] = TextParam
				.builder()
				.collectTime(strategyTime)
				.name(STATUS_INFORMATION_PARAMETER)
				.value(success ? "Connector test succeeded." : "Connector test failed.")
				.build();

		return statusAndStatusInformation;
	}

	/**
	 * Build test report parameter for the given {@link TestedConnector}.
	 *
	 * @param hostName		    The name of the host against which the test has been performed.
	 * @param testedConnector	The {@link TestedConnector} whose test report parameter should be created.
	 *
	 * @return					A {@link TextParam} instance built from the given {@link TestedConnector}.
	 */
	protected TextParam buildTestReportParameter(final String hostName, final TestedConnector testedConnector) {
		final TextParam testReport = TextParam
				.builder()
				.collectTime(strategyTime)
				.name(TEST_REPORT_PARAMETER)
				.build();

		final StringBuilder value = new StringBuilder();

		final String builtTestResult = testedConnector.getCriterionTestResults().stream()
						.map(criterionResult -> {
							final String result = criterionResult.getResult();
							String message = criterionResult.getMessage();
							return String.format("Received Result: %s. %s", result != null ? result : N_A,
									message != null ? message : N_A);
						})
						.collect(Collectors.joining("\n"));
		value.append(builtTestResult)
				.append("\nConclusion: ")
				.append("TEST on ")
				.append(hostName)
				.append(WHITE_SPACE)
				.append(getTestedConnectorStatus(testedConnector));

		testReport.setValue(value.toString());

		return testReport;
	}

	/**
	 * @param hostMonitoring The {@link IHostMonitoring} instance.
	 *
	 * @return	The host {@link Monitor} in the given {@link IHostMonitoring} instance.
	 */
	protected Monitor getHostMonitor(IHostMonitoring hostMonitoring) {

		Monitor host = hostMonitoring.getHostMonitor();
		state(host != null, "Host monitor should not be null.");

		return host;
	}

	/**
	 * Get the temperature threshold value from the given metadata map
	 *
	 * @param metadata The {@link Monitor}'s metadata.
	 * @return Double value
	 */
	protected Double getTemperatureWarningThreshold(@NonNull final Map<String, String> metadata) {
		final String warningThresholdMetadata = metadata.get(WARNING_THRESHOLD);
		final String alarmThresholdMetadata = metadata.get(ALARM_THRESHOLD);

		final Double warningThreshold = NumberHelper.parseDouble(warningThresholdMetadata, null);
		final Double alarmThreshold = NumberHelper.parseDouble(alarmThresholdMetadata, null);

		// If we only have an alarm threshold, then warningThreshold will be 90% of alarmThreshold
		// If we only have a warning threshold, we are good.
		// If we have both warning and alarm threshold then we return the minimum value
		if (warningThreshold == null && alarmThreshold != null) {
			return NumberHelper.round(alarmThreshold * 0.9, 1, RoundingMode.HALF_UP);
		} else if (warningThreshold != null && alarmThreshold == null) {
			return warningThreshold;
		} else if (warningThreshold != null) {
			// return the minimum between warning and alarm
			return Math.min(warningThreshold, alarmThreshold);
		}

		return null;
	}
}
