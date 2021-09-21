package com.sentrysoftware.matrix.engine.strategy;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.N_A;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;
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
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
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
	 * @param hostname       The hostname of the target only used for logging
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
	 * @param hostname       The hostname of the target only used for logging
	 * @param monitor        The monitor used in the mono instance processing
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname, final Monitor monitor) {

		if (sources == null || sources.isEmpty()) {
			log.debug("No source found from connector {} with monitor {}. System {}", connector.getCompiledFilename(), monitorType, hostname);
			return;
		}

		// Loop over all the sources and accept the SourceVisitor which is going to
		// visit and process the source
		for (final Source source : sources) {

			final ISourceVisitor sourceVisitor = new SourceVisitor(strategyConfig, matsyaClientsExecutor, connector);
			final SourceTable sourceTable;

			final Supplier<SourceTable> executable = () -> 
				source.accept(new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig));

			if (source.isForceSerialization()) {
				sourceTable = forceSerialization(executable, connector, source, "source", SourceTable.empty());
			} else {
				sourceTable = executable.get();
			}

			if (sourceTable == null) {
				log.warn("Received null source table for source key {}. Connector {}. Monitor {}. System {}",
						source.getKey(),
						connector.getCompiledFilename(),
						monitorType,
						hostname);
				continue;
			}

			final List<Compute> computes = source.getComputes();

			if (computes != null) {

				final ComputeVisitor computeVisitor = new ComputeVisitor(sourceTable, connector, matsyaClientsExecutor);
				final ComputeUpdaterVisitor computeUpdaterVisitor = new ComputeUpdaterVisitor(computeVisitor, monitor);

				for (final Compute compute : computes) {
					compute.accept(computeUpdaterVisitor);
				}

				hostMonitoring
						.getConnectorNamespace(connector)
						.addSourceTable(source.getKey(), computeVisitor.getSourceTable());
			} else {
				hostMonitoring
						.getConnectorNamespace(connector)
						.addSourceTable(source.getKey(), sourceTable);
			}
		}
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
			log.warn("The connector {} DOES NOT match {}'s platform as it has no detection to test.",
					connector.getCompiledFilename(), hostname);
			return testedConnector;
		}

		final List<Criterion> criteria = detection.getCriteria();

		if (criteria == null || criteria.isEmpty()) {
			log.warn("The connector {} DOES NOT match {}'s platform as it has no criteria to test.",
					connector.getCompiledFilename(), hostname);
			return testedConnector;
		}

		for (Criterion criterion : criteria) {
			final CriterionTestResult critetionTestResult = processCriterion(criterion, connector);
			if (!critetionTestResult.isSuccess()) {
				log.debug("Detected failed criterion for connector {} on platform: {}. Message: {}.",
						connector.getCompiledFilename(),
						hostname,
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

		final boolean isLockAcquired;
		try {
			// Try to get the lock
			isLockAcquired = forceSerializationLock.tryLock(DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Interrupted exception detected when trying to acquire the force serialization lock to process {} {}. Connector: {}.",
					description,
					objToProcess,
					connector.getCompiledFilename());
			log.debug("Exception: ", e);

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
			log.error("Could not acquire the force serialization lock to process {} {}. Connector: {}.",
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
	 * Build status parameter for the given {@link TestedConnector}.
	 *
	 * @param testedConnector	The {@link TestedConnector} whose status should be created.
	 *
	 * @return					A {@link StatusParam} instance built from the given {@link TestedConnector}.
	 */
	protected StatusParam buildStatusParamForConnector(final TestedConnector testedConnector) {
		boolean success = testedConnector.isSuccess();
		return StatusParam
				.builder()
				.collectTime(strategyTime)
				.name(STATUS_PARAMETER)
				.state(success ? ParameterState.OK : ParameterState.ALARM)
				.statusInformation(success ? "Connector test succeeded" : "Connector test failed")
				.unit(STATUS_PARAMETER_UNIT)
				.build();
	}

	/**
	 * Build test report parameter for the given {@link TestedConnector}.
	 *
	 * @param targetName		The name of the target against which the test has been performed.
	 * @param testedConnector	The {@link TestedConnector} whose test report parameter should be created.
	 *
	 * @return					A {@link TextParam} instance built from the given {@link TestedConnector}.
	 */
	protected TextParam buildTestReportParameter(final String targetName, final TestedConnector testedConnector) {
		final TextParam testReport = TextParam
				.builder()
				.collectTime(strategyTime)
				.name(TEST_REPORT_PARAMETER)
				.parameterState(ParameterState.OK)
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
				.append(targetName)
				.append(WHITE_SPACE)
				.append(getTestedConnectorStatus(testedConnector));

		testReport.setValue(value.toString());

		return testReport;
	}

	/**
	 * @param hostMonitoring The {@link IHostMonitoring} instance.
	 *
	 * @return	The target {@link Monitor} in the given {@link IHostMonitoring} instance.
	 */
	protected Monitor getTargetMonitor(IHostMonitoring hostMonitoring) {

		final Map<String, Monitor> targetMonitors = hostMonitoring.selectFromType(MonitorType.TARGET);
		state(targetMonitors != null && !targetMonitors.isEmpty(), "targetMonitors should not be null or empty.");

		return targetMonitors
			.values()
			.stream()
			.findFirst()
			.orElseThrow();
	}

	/**
	 * Get the temperature threshold value from the given metadata map
	 * 
	 * @param metadata The {@link Monitor}'s metadata.
	 * @return Double value
	 */
	protected Double getTemperatureWarningThreshold(@NonNull final Map<String, String> metadata) {
		final String warningThresholdMetadata = metadata.get(WARNING_THRESHOLD);
		final String alamThresholdMetadata = metadata.get(ALARM_THRESHOLD);

		final Double warningThreshold = NumberHelper.parseDouble(warningThresholdMetadata, null);
		final Double alarmThreshold = NumberHelper.parseDouble(alamThresholdMetadata, null);

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
