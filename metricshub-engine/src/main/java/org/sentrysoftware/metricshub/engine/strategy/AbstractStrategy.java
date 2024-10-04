package org.sentrysoftware.metricshub.engine.strategy;

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
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.LOG_COMPUTE_KEY_SUFFIX_TEMPLATE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_FAILED;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.STATE_SET_METRIC_OK;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.JobInfo;
import org.sentrysoftware.metricshub.engine.common.exception.RetryableException;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.detection.ConnectorSelection;
import org.sentrysoftware.metricshub.engine.strategy.detection.ConnectorTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceUpdaterProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.ComputeProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.compute.ComputeUpdaterProcessor;
import org.sentrysoftware.metricshub.engine.strategy.utils.ForceSerializationHelper;
import org.sentrysoftware.metricshub.engine.strategy.utils.RetryOperation;
import org.sentrysoftware.metricshub.engine.telemetry.ConnectorNamespace;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Abstract class representing a strategy for handling connectors and their sources and computes.
 */
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
	protected ClientsExecutor clientsExecutor;

	@NonNull
	protected ExtensionManager extensionManager;

	private static final String COMPUTE = "compute";
	private static final String SOURCE = "source";

	/**
	 * Format for string value like: <em>connector_connector-id</em>
	 */
	public static final String CONNECTOR_ID_FORMAT = "%s_%s";

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
		final String connectorId = jobInfo.getConnectorId();
		final String monitorType = jobInfo.getMonitorType();
		final String hostname = jobInfo.getHostname();

		if (sources == null || sources.isEmpty()) {
			log.debug(
				"Hostname {} - No sources found from connector {} with monitor {}.",
				hostname,
				connectorId,
				monitorType
			);
			return;
		}

		// Loop over all the sources and accept the SourceProcessor which is going to
		// process the source
		for (final Source source : sources) {
			final String sourceKey = source.getKey();

			logBeginOperation(SOURCE, source, sourceKey, connectorId, hostname);

			final SourceTable previousSourceTable = telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorId)
				.getSourceTable(sourceKey);

			// Execute the source and retry the operation
			// in case the source fails but the previous source table didn't fail
			SourceTable sourceTable = RetryOperation
				.<SourceTable>builder()
				.withDefaultValue(SourceTable.empty())
				.withMaxRetries(1)
				.withWaitStrategy(telemetryManager.getHostConfiguration().getRetryDelay())
				.withDescription(String.format("%s [%s]", SOURCE, sourceKey))
				.withHostname(hostname)
				.build()
				.run(() -> runSource(connectorId, attributes, source, previousSourceTable));

			final boolean isNullSourceTable = sourceTable == null;
			if (isNullSourceTable || sourceTable.isEmpty()) {
				log.warn(
					"Hostname {} - Received {} source table for Source key {} - Connector {} - Monitor {}. The source table is set to empty.",
					hostname,
					isNullSourceTable ? "null" : "empty",
					sourceKey,
					connectorId,
					monitorType
				);
				// This ensures that the internal table (List<List<String>>) is not null and rawData integrity is maintained
				sourceTable = SourceTable.builder().rawData(sourceTable.getRawData()).table(new ArrayList<>()).build();
			}

			// log the source table
			logSourceTable(SOURCE, source.getClass().getSimpleName(), sourceKey, connectorId, sourceTable, hostname);

			final List<Compute> computes = source.getComputes();

			// Add the source table and stop if no compute is found
			if (computes == null || computes.isEmpty()) {
				telemetryManager.getHostProperties().getConnectorNamespace(connectorId).addSourceTable(sourceKey, sourceTable);
				continue;
			}

			final ComputeProcessor computeProcessor = ComputeProcessor
				.builder()
				.sourceKey(sourceKey)
				.sourceTable(sourceTable)
				.connectorId(connectorId)
				.hostname(hostname)
				.clientsExecutor(clientsExecutor)
				.telemetryManager(telemetryManager)
				.build();

			final ComputeUpdaterProcessor computeUpdaterProcessor = ComputeUpdaterProcessor
				.builder()
				.computeProcessor(computeProcessor)
				.attributes(attributes)
				.connectorId(connectorId)
				.telemetryManager(telemetryManager)
				.build();

			// Loop over the computes to process each compute
			for (int index = 0; index < computes.size(); index++) {
				final Compute compute = computes.get(index);
				computeProcessor.setIndex(index);

				final String computeKey = String.format(LOG_COMPUTE_KEY_SUFFIX_TEMPLATE, sourceKey, index);

				logBeginOperation(COMPUTE, compute, computeKey, connectorId, hostname);

				// process the compute
				compute.accept(computeUpdaterProcessor);

				// log the updated source table
				logSourceTable(
					COMPUTE,
					compute.getClass().getSimpleName(),
					computeKey,
					connectorId,
					computeProcessor.getSourceTable(),
					hostname
				);
			}

			telemetryManager
				.getHostProperties()
				.getConnectorNamespace(connectorId)
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
	 * @param connectorId         The connector compiled filename (identifier) we currently process
	 * @param attributes          Key-value pairs of the monitor's attributes used
	 *                            in the mono instance processing
	 * @param source              The source we want to run
	 * @param previousSourceTable The source result produced in the past
	 * @return new {@link SourceTable} instance
	 */
	private SourceTable runSource(
		final String connectorId,
		final Map<String, String> attributes,
		final Source source,
		final SourceTable previousSourceTable
	) {
		final ISourceProcessor sourceProcessor = SourceProcessor
			.builder()
			.connectorId(connectorId)
			.clientsExecutor(clientsExecutor)
			.telemetryManager(telemetryManager)
			.extensionManager(extensionManager)
			.build();

		final Supplier<SourceTable> executable = () ->
			source.accept(
				SourceUpdaterProcessor
					.builder()
					.connectorId(connectorId)
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
					connectorId,
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
	 * @param connectorId   the connector identifier
	 * @param hostname      the hostname
	 */
	private static <T> void logBeginOperation(
		final String operationTag,
		final T execution,
		final String executionKey,
		final String connectorId,
		final String hostname
	) {
		if (!log.isInfoEnabled()) {
			return;
		}

		log.info(
			"Hostname {} - Begin {} [{} {}] for connector [{}]:\n{}\n",
			hostname,
			operationTag,
			execution.getClass().getSimpleName(),
			executionKey,
			connectorId,
			execution.toString()
		);
	}

	/**
	 * Log the {@link SourceTable} result.
	 *
	 * @param operationTag   the tag of the operation. E.g. source or compute
	 * @param executionClassName the source or the compute class name we want to log
	 * @param executionKey   the key of the source or the compute we want to log
	 * @param connectorId    the compiled file name of the connector (identifier)
	 * @param sourceTable    the source's result we wish to log
	 * @param hostname       the hostname of the source we wish to log
	 */
	static void logSourceTable(
		final String operationTag,
		final String executionClassName,
		final String executionKey,
		final String connectorId,
		final SourceTable sourceTable,
		final String hostname
	) {
		if (!log.isInfoEnabled()) {
			return;
		}

		// Is there any raw data to log?
		if (sourceTable.getRawData() != null && (sourceTable.getTable() == null || sourceTable.getTable().isEmpty())) {
			log.info(
				"Hostname {} - End of {} [{} {}] for connector [{}].\nRaw result:\n{}\n",
				hostname,
				operationTag,
				executionClassName,
				executionKey,
				connectorId,
				sourceTable.getRawData()
			);
			return;
		}

		if (sourceTable.getRawData() == null) {
			log.info(
				"Hostname {} - End of {} [{} {}] for connector [{}].\nTable result:\n{}\n",
				hostname,
				operationTag,
				executionClassName,
				executionKey,
				connectorId,
				TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
			);
			return;
		}

		log.info(
			"Hostname {} - End of {} [{} {}] for connector [{}].\nRaw result:\n{}\nTable result:\n{}\n",
			hostname,
			operationTag,
			executionClassName,
			executionKey,
			connectorId,
			sourceTable.getRawData(),
			TextTableHelper.generateTextTable(sourceTable.getHeaders(), sourceTable.getTable())
		);
	}

	@Override
	public long getStrategyTimeout() {
		return telemetryManager.getHostConfiguration().getStrategyTimeout();
	}

	/**
	 * Validates the connector's detection criteria
	 *
	 * @param currentConnector	Connector instance
	 * @param hostname			Hostname
	 * @return					boolean representing the success of the tests
	 */
	protected boolean validateConnectorDetectionCriteria(final Connector currentConnector, final String hostname) {
		if (currentConnector.getConnectorIdentity().getDetection() == null) {
			return true;
		}

		final ConnectorTestResult connectorTestResult = new ConnectorSelection(
			telemetryManager,
			clientsExecutor,
			Collections.emptySet(),
			extensionManager
		)
			.runConnectorDetectionCriteria(currentConnector, hostname);
		final String connectorId = currentConnector.getCompiledFilename();
		final Monitor monitor = telemetryManager.findMonitorByTypeAndId(
			KnownMonitorType.CONNECTOR.getKey(),
			String.format(CONNECTOR_ID_FORMAT, KnownMonitorType.CONNECTOR.getKey(), connectorId)
		);

		// Add statusInformation to legacyTextParameters attribute of the connector monitor
		final String statusInformation = buildStatusInformation(hostname, connectorTestResult);
		final Map<String, String> legacyTextParameters = monitor.getLegacyTextParameters();
		legacyTextParameters.put("StatusInformation", statusInformation);

		collectConnectorStatus(connectorTestResult.isSuccess(), connectorId, monitor);
		return connectorTestResult.isSuccess();
	}

	/**
	 * Builds the status information for the connector
	 * @param hostname   Hostname of the resource being monitored
	 * @param testResult Test result of the connector
	 * @return String representing the status information
	 */
	protected String buildStatusInformation(final String hostname, final ConnectorTestResult testResult) {
		final StringBuilder value = new StringBuilder();

		final String builtTestResult = testResult
			.getCriterionTestResults()
			.stream()
			.filter(criterionTestResult ->
				!(criterionTestResult.getResult() == null && criterionTestResult.getMessage() == null)
			)
			.map(criterionResult -> {
				final String result = criterionResult.getResult();
				final String message = criterionResult.getMessage();
				return String.format(
					"Received Result: %s. %s",
					result != null ? result : "N/A",
					message != null ? message : "N/A"
				);
			})
			.collect(Collectors.joining("\n"));
		value
			.append(builtTestResult)
			.append("\nConclusion: ")
			.append("Test on ")
			.append(hostname)
			.append(" ")
			.append(testResult.isSuccess() ? "SUCCEEDED" : "FAILED");

		return value.toString();
	}

	/**
	 * Collects the connector status and sets the metric
	 *
	 * @param isSuccessCriteria Whether the connector's criteria are successfully executed or not
	 * @param connectorId       Connector ID
	 * @param monitor           Monitor instance
	 */
	protected void collectConnectorStatus(
		final boolean isSuccessCriteria,
		final String connectorId,
		final Monitor monitor
	) {
		// Initialize the metric factory to collect metrics
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());

		// Get the connector's namespace containing related settings
		final ConnectorNamespace connectorNamespace = telemetryManager
			.getHostProperties()
			.getConnectorNamespace(connectorId);

		// Collect the metric
		metricFactory.collectStateSetMetric(
			monitor,
			CONNECTOR_STATUS_METRIC_KEY,
			isSuccessCriteria ? STATE_SET_METRIC_OK : STATE_SET_METRIC_FAILED,
			new String[] { STATE_SET_METRIC_OK, STATE_SET_METRIC_FAILED },
			strategyTime
		);

		// Set isStatusOk to true in ConnectorNamespace
		connectorNamespace.setStatusOk(isSuccessCriteria);
	}
}
