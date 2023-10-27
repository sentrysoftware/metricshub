package com.sentrysoftware.metricshub.engine.strategy;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.LOG_COMPUTE_KEY_SUFFIX_TEMPLATE;

import com.sentrysoftware.metricshub.engine.common.JobInfo;
import com.sentrysoftware.metricshub.engine.common.exception.RetryableException;
import com.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceUpdaterProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.compute.ComputeProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.compute.ComputeUpdaterProcessor;
import com.sentrysoftware.metricshub.engine.strategy.utils.ForceSerializationHelper;
import com.sentrysoftware.metricshub.engine.strategy.utils.RetryOperation;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public abstract class AbstractStrategy implements IStrategy {

	@NonNull
	protected TelemetryManager telemetryManager;

	@NonNull
	protected Long strategyTime;

	@NonNull
	protected MatsyaClientsExecutor matsyaClientsExecutor;

	private static final String COMPUTE = "compute";
	private static final String SOURCE = "source";

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link TelemetryManager}
	 *
	 * @param sources The {@link List} of {@link Source} instances we wish to execute
	 * @param jobInfo Information about the job such as hostname, monitorType, job name and connectorName.
	 */
	protected void processSourcesAndComputes(final List<Source> sources, final JobInfo jobInfo) {
		processSourcesAndComputes(sources, null, jobInfo);
	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link TelemetryManager}
	 *
	 * @param sources    The {@link List} of {@link Source} instances we wish to execute
	 * @param attributes Key-value pairs of the monitor's attributes used in the mono instance processing
	 * @param jobInfo    Information about the job such as hostname, monitorType, job name and connectorName.
	 */
	protected void processSourcesAndComputes(
		final List<Source> sources,
		final Map<String, String> attributes,
		final JobInfo jobInfo
	) {
		final String connectorName = jobInfo.getConnectorName();
		final String monitorType = jobInfo.getMonitorType();
		final String hostname = jobInfo.getHostname();

		if (sources == null || sources.isEmpty()) {
			log.debug(
				"Hostname {} - No sources found from connector {} with monitor {}.",
				hostname,
				connectorName,
				monitorType
			);
			return;
		}

		// Loop over all the sources and accept the SourceProcessor which is going to
		// process the source
		for (final Source source : sources) {
			final String sourceKey = source.getKey();

			logBeginOperation(SOURCE, source, sourceKey, connectorName, hostname);

			final SourceTable previousSourceTable = telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorName)
				.getSourceTable(sourceKey);

			// Execute the source and retry the operation
			// in case the source fails but the previous source table didn't fail
			final SourceTable sourceTable = RetryOperation
				.<SourceTable>builder()
				.withDefaultValue(SourceTable.empty())
				.withMaxRetries(1)
				.withWaitStrategy(telemetryManager.getHostConfiguration().getRetryDelay())
				.withDescription(String.format("%s [%s]", SOURCE, sourceKey))
				.withHostname(hostname)
				.build()
				.run(() -> runSource(connectorName, attributes, source, previousSourceTable));

			if (sourceTable == null) {
				log.warn(
					"Hostname {} - Received null source table for Source key {} - Connector {} - Monitor {}.",
					hostname,
					sourceKey,
					connectorName,
					monitorType
				);
				continue;
			}

			// log the source table
			logSourceTable(SOURCE, source.getClass().getSimpleName(), sourceKey, connectorName, sourceTable, hostname);

			final List<Compute> computes = source.getComputes();

			// Add the source table and stop if no compute is found
			if (computes == null || computes.isEmpty()) {
				telemetryManager
					.getHostProperties()
					.getConnectorNamespace(connectorName)
					.addSourceTable(sourceKey, sourceTable);
				return;
			}

			final ComputeProcessor computeProcessor = ComputeProcessor
				.builder()
				.sourceKey(sourceKey)
				.sourceTable(sourceTable)
				.connectorName(connectorName)
				.hostname(hostname)
				.matsyaClientsExecutor(matsyaClientsExecutor)
				.telemetryManager(telemetryManager)
				.build();

			final ComputeUpdaterProcessor computeUpdaterProcessor = ComputeUpdaterProcessor
				.builder()
				.computeProcessor(computeProcessor)
				.attributes(attributes)
				.connectorName(connectorName)
				.telemetryManager(telemetryManager)
				.build();

			// Loop over the computes to process each compute
			for (int index = 0; index < computes.size(); index++) {
				final Compute compute = computes.get(index);
				computeProcessor.setIndex(index);

				final String computeKey = String.format(LOG_COMPUTE_KEY_SUFFIX_TEMPLATE, sourceKey, index);

				logBeginOperation(COMPUTE, compute, computeKey, connectorName, hostname);

				// process the compute
				compute.accept(computeUpdaterProcessor);

				// log the updated source table
				logSourceTable(
					COMPUTE,
					compute.getClass().getSimpleName(),
					computeKey,
					connectorName,
					computeProcessor.getSourceTable(),
					hostname
				);
			}

			telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorName)
				.addSourceTable(sourceKey, computeProcessor.getSourceTable());
		}
	}

	/**
	 * Whether the given source table is empty or not
	 *
	 * @param sourceTable The result produced after executing a source
	 * @return boolean value
	 */
	private boolean isNullOrEmptySourceTable(final SourceTable sourceTable) {
		return sourceTable == null || sourceTable.isEmpty();
	}

	/**
	 * Execute the given source. If the source is marked as serializable
	 * (ForceSerialization) The execution will be performed through
	 * <code>forceSerialization(...)</code> method.
	 *
	 * @param connectorCompiledFilename The connector compiled filename we currently process
	 * @param attributes                Key-value pairs of the monitor's attributes used
	 *                                  in the mono instance processing
	 * @param source                    The source we want to run
	 * @param previousSourceTable       The source result produced in the past
	 * @return new {@link SourceTable} instance
	 */
	private SourceTable runSource(
		final String connectorCompiledFilename,
		final Map<String, String> attributes,
		final Source source,
		final SourceTable previousSourceTable
	) {
		final ISourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.connectorName(connectorCompiledFilename)
			.matsyaClientsExecutor(matsyaClientsExecutor)
			.telemetryManager(telemetryManager)
			.build();

		final Supplier<SourceTable> executable = () ->
			source.accept(
				SourceUpdaterProcessor
					.builder()
					.connectorName(connectorCompiledFilename)
					.sourceProcessor(sourceProcessor)
					.telemetryManager(telemetryManager)
					.attributes(attributes)
					.build()
			);

		// Process the source to get a source table

		final SourceTable sourceTable;

		if (source.isForceSerialization()) {
			sourceTable =
				ForceSerializationHelper.forceSerialization(
					executable,
					telemetryManager,
					connectorCompiledFilename,
					source,
					SOURCE,
					SourceTable.empty()
				);
		} else {
			sourceTable = executable.get();
		}

		// A retry must be attempted if this source has already produced results in the past
		if (!isNullOrEmptySourceTable(previousSourceTable) && isNullOrEmptySourceTable(sourceTable)) {
			throw new RetryableException();
		}

		return sourceTable;
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
	private static <T> void logBeginOperation(
		final String operationTag,
		final T execution,
		final String executionKey,
		final String connectorName,
		final String hostname
	) {
		if (!log.isInfoEnabled()) {
			return;
		}

		log.info(
			"Hostname {} - Begin {} [{} {}] for hardware connector [{}]:\n{}\n",
			hostname,
			operationTag,
			execution.getClass().getSimpleName(),
			executionKey,
			connectorName,
			execution.toString()
		);
	}

	/**
	 * Log the {@link SourceTable} result.
	 *
	 * @param operationTag   the tag of the operation. E.g. source or compute
	 * @param executionClassName the source or the compute class name we want to log
	 * @param executionKey   the key of the source or the compute we want to log
	 * @param connectorName  the compiled file name of the connector
	 * @param sourceTable    the source's result we wish to log
	 * @param hostname       the hostname of the source we wish to log
	 */
	static void logSourceTable(
		final String operationTag,
		final String executionClassName,
		final String executionKey,
		final String connectorName,
		final SourceTable sourceTable,
		final String hostname
	) {
		if (!log.isInfoEnabled()) {
			return;
		}

		// Is there any raw data to log?
		if (sourceTable.getRawData() != null && (sourceTable.getTable() == null || sourceTable.getTable().isEmpty())) {
			log.info(
				"Hostname {} - End of {} [{} {}] for hardware connector [{}].\nRaw result:\n{}\n",
				hostname,
				operationTag,
				executionClassName,
				executionKey,
				connectorName,
				sourceTable.getRawData()
			);
			return;
		}

		if (sourceTable.getRawData() == null) {
			log.info(
				"Hostname {} - End of {} [{} {}] for hardware connector [{}].\nTable result:\n{}\n",
				hostname,
				operationTag,
				executionClassName,
				executionKey,
				connectorName,
				TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
			);
			return;
		}

		log.info(
			"Hostname {} - End of {} [{} {}] for hardware connector [{}].\nRaw result:\n{}\nTable result:\n{}\n",
			hostname,
			operationTag,
			executionClassName,
			executionKey,
			connectorName,
			sourceTable.getRawData(),
			TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
		);
	}

	@Override
	public long getStrategyTimeout() {
		return telemetryManager.getHostConfiguration().getStrategyTimeout();
	}
}