package com.sentrysoftware.matrix.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.KnownMonitorType.HOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MAX_THREADS_COUNT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_JOBS_PRIORITY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTHER_MONITOR_JOB_TYPES;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PRESENT_STATUS;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.THREAD_TIMEOUT;

import com.sentrysoftware.matrix.common.ConnectorMonitorTypeComparator;
import com.sentrysoftware.matrix.common.JobInfo;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AbstractCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.connector.model.monitor.task.MultiInstanceCollect;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.strategy.source.OrderedSources;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.strategy.utils.MappingProcessor;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.AbstractMetric;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectStrategy extends AbstractStrategy {

	public CollectStrategy(
		@NonNull final TelemetryManager telemetryManager,
		final long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);
	}

	@Override
	public void prepare() {
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.forEach(monitor ->
				monitor
					.getMetrics()
					.values()
					.stream()
					.filter(AbstractMetric::isResetMetricTime)
					.forEach(metric -> metric.setCollectTime(strategyTime))
			);
	}

	/**
	 * This method collects the monitors and their metrics
	 * @param currentConnector Connector instance
	 * @param hostname the host name
	 */
	private void collect(final Connector currentConnector, final String hostname) {
		// Sort the connector monitor jobs according to the priority map
		final Map<String, MonitorJob> connectorMonitorJobs = currentConnector
			.getMonitors()
			.entrySet()
			.stream()
			.sorted(
				Comparator.comparing(entry ->
					MONITOR_JOBS_PRIORITY.containsKey(entry.getKey())
						? MONITOR_JOBS_PRIORITY.get(entry.getKey())
						: MONITOR_JOBS_PRIORITY.get(OTHER_MONITOR_JOB_TYPES)
				)
			)
			.collect(
				Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new)
			);

		final Map<String, MonitorJob> sequentialMonitorJobs = connectorMonitorJobs
			.entrySet()
			.stream()
			.filter(entry -> MONITOR_JOBS_PRIORITY.containsKey(entry.getKey()))
			.collect(
				Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new)
			);

		final Map<String, MonitorJob> otherMonitorJobs = connectorMonitorJobs
			.entrySet()
			.stream()
			.filter(entry -> !MONITOR_JOBS_PRIORITY.containsKey(entry.getKey()))
			.collect(
				Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new)
			);

		// Run monitor jobs defined in monitor jobs priority map (host, enclosure, blade, disk_controller and cpu)  in sequential mode
		sequentialMonitorJobs.entrySet().forEach(entry -> processMonitorJob(currentConnector, hostname, entry));

		// If monitor jobs execution is set to "sequential", execute monitor jobs one by one
		if (telemetryManager.getHostConfiguration().isSequential()) {
			otherMonitorJobs.entrySet().forEach(entry -> processMonitorJob(currentConnector, hostname, entry));
		} else {
			// Execute monitor jobs in parallel
			log.info(
				"Hostname {} - Running collect in parallel mode. Connector: {}.",
				hostname,
				currentConnector.getConnectorIdentity().getCompiledFilename()
			);

			final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

			otherMonitorJobs
				.entrySet()
				.forEach(entry -> threadsPool.execute(() -> processMonitorJob(currentConnector, hostname, entry)));

			// Order the shutdown
			threadsPool.shutdown();

			try {
				// Blocks until all tasks have completed execution after a shutdown request
				threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
			} catch (Exception e) {
				if (e instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				log.debug("Hostname {} - Waiting for threads' termination aborted with an error.", hostname, e);
			}
		}
	}

	/**
	 * This method processes a monitor job
	 *
	 * @param currentConnector The current connector we process its monitor job
	 * @param hostname         The host name we currently monitor
	 * @param monitorJob       The monitor job instance we wish to process
	 */
	private void processMonitorJob(
		final Connector currentConnector,
		final String hostname,
		final Map.Entry<String, MonitorJob> monitorJob
	) {
		if (monitorJob.getValue() instanceof StandardMonitorJob standardMonitorJob) {
			final AbstractCollect collect = standardMonitorJob.getCollect();

			// Check whether collect is null
			if (collect == null) {
				return;
			}

			final String monitorType = monitorJob.getKey();

			final JobInfo jobInfo = JobInfo
				.builder()
				.hostname(hostname)
				.connectorName(currentConnector.getCompiledFilename())
				.jobName(collect.getClass().getSimpleName())
				.monitorType(monitorType)
				.build();

			// Build the ordered sources
			final OrderedSources orderedSources = OrderedSources
				.builder()
				.sources(collect.getSources(), collect.getExecutionOrder().stream().toList(), collect.getSourceDep(), jobInfo)
				.build();

			if (collect instanceof MultiInstanceCollect multiInstanceCollect) {
				final Map<String, Monitor> monitors = telemetryManager.findMonitorByType(monitorType);
				if (monitors == null) {
					return;
				}

				// Create the sources and the computes for a connector
				processSourcesAndComputes(orderedSources.getSources(), jobInfo);

				processMonitors(
					monitorType,
					multiInstanceCollect.getMapping(),
					currentConnector,
					hostname,
					multiInstanceCollect.getKeys()
				);
			} else {
				// Get monitors by type and connectorId (connector id attribute)
				final Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorType);

				final Map<String, Monitor> sameTypeSameConnectorMonitors = sameTypeMonitors
					.values()
					.stream()
					.filter(monitor ->
						currentConnector.getCompiledFilename().equals(monitor.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID))
					)
					.collect(
						Collectors.toMap(
							monitorEntry -> monitorEntry.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID),
							monitorEntry -> monitorEntry,
							(oldValue, newValue) -> oldValue,
							LinkedHashMap::new
						)
					);

				// Loop on each monitor
				sameTypeSameConnectorMonitors
					.values()
					.stream()
					.forEach(monitor -> {
						processSourcesAndComputes(orderedSources.getSources(), monitor.getAttributes(), jobInfo);
						processMonitors(monitorType, collect.getMapping(), currentConnector, hostname, monitor);
					});
			}
		}
	}

	/**
	 * This method processes multi instances collect
	 *
	 * @param monitorType Type of the monitor
	 * @param mapping     The collect's mapping used to collect metrics
	 * @param connector   The current connector instance
	 * @param hostname    The host name we currently monitor
	 * @param keys        {@link Set} of attribute keys used to find the monitor to collect
	 */
	private void processMonitors(
		final String monitorType,
		final Mapping mapping,
		final Connector connector,
		final String hostname,
		final Set<String> keys
	) {
		processMonitors(monitorType, mapping, connector, hostname, Optional.empty(), keys);
	}

	/**
	 * This method processes a mono instance collect
	 *
	 * @param monitorType Type of the monitor
	 * @param mapping     The collect's mapping used to collect metrics
	 * @param connector   The current connector instance
	 * @param hostname    The host name we currently monitor
	 * @param monitor     {@link Monitor} instance collect (mono instance)
	 */
	private void processMonitors(
		final String monitorType,
		final Mapping mapping,
		final Connector connector,
		final String hostname,
		final Monitor monitor
	) {
		processMonitors(monitorType, mapping, connector, hostname, Optional.of(monitor), null);
	}

	/**
	 * This method processes multi instances or mono instance monitor collect
	 *
	 * @param monitorType     type of the monitor
	 * @param mapping         the collect's mapping used to collect metrics
	 * @param connector       a given connector
	 * @param hostname        the host name we currently monitor
	 * @param maybeMonitor    empty in case of multi-instance processing otherwise an {@link Optional} of an existing
	 *                        {@link Monitor} instance used to process the mono instance collect
	 * @param attributeKeys  null in case of mono-instance processing  otherwise a {@link Set} of attribute keys
	 *                       used to find the monitor to collect in multi-instance mode
	 */
	private void processMonitors(
		final String monitorType,
		final Mapping mapping,
		final Connector connector,
		final String hostname,
		final Optional<Monitor> maybeMonitor,
		final Set<String> attributeKeys
	) {
		if (mapping == null) {
			return;
		}

		final String connectorId = connector.getCompiledFilename();

		final String mappingSource = mapping.getSource();

		final Optional<SourceTable> maybeSourceTable = SourceTable.lookupSourceTable(
			mappingSource,
			connectorId,
			telemetryManager
		);

		// No sourceTable no monitor
		if (maybeSourceTable.isEmpty()) {
			log.debug(
				"Hostname {} - Collect - No source table created with source key {} for connector {}.",
				hostname,
				mappingSource,
				connectorId
			);
			return;
		}

		final List<List<String>> table = maybeSourceTable.get().getTable();

		if (table.isEmpty()) {
			return;
		}

		// If we process single monitor (monoInstance), we loop until first row.
		// Otherwise, (in case of multi-instance processing), we loop over all the source table rows
		final int rowCountLimit = maybeMonitor.isEmpty() ? table.size() : 1;

		final Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorType);

		// Loop over the source table rows
		for (int i = 0; i < rowCountLimit; i++) {
			final List<String> row = table.get(i);

			// Init mapping processor
			final MappingProcessor mappingProcessor = MappingProcessor
				.builder()
				.telemetryManager(telemetryManager)
				.mapping(mapping)
				.jobInfo(
					JobInfo
						.builder()
						.connectorName(connectorId)
						.hostname(hostname)
						.monitorType(monitorType)
						.jobName("collect")
						.build()
				)
				.collectTime(strategyTime)
				.row(row)
				.build();

			// In case of multi-instance, maybeMonitor is empty. So, we try to find it by type, connector id and attribute keys
			maybeMonitor
				.or(() ->
					findMonitor(
						connectorId,
						sameTypeMonitors,
						mappingProcessor.interpretNonContextMappingAttributes(),
						attributeKeys
					)
				)
				.ifPresent(monitor -> {
					// Collect metrics
					final Map<String, String> metrics = mappingProcessor.interpretNonContextMappingMetrics();

					metrics.putAll(mappingProcessor.interpretContextMappingMetrics(monitor));

					final MetricFactory metricFactory = new MetricFactory(telemetryManager);

					metricFactory.collectMonitorMetrics(
						monitorType,
						connector,
						hostname,
						monitor,
						connectorId,
						metrics,
						strategyTime,
						false
					);

					// Collect legacy parameters
					monitor.addLegacyParameters(mappingProcessor.interpretNonContextMappingLegacyTextParameters());
					monitor.addLegacyParameters(mappingProcessor.interpretContextMappingLegacyTextParameters(monitor));
				});
		}
	}

	/**
	 * Find monitor by attributes keys and connector identifier
	 *
	 * @param connectorId               Unique connector identifier used to find the monitor
	 * @param sameTypeMonitors          {@link Monitor} instances having the same type
	 * @param collectedAttributeValues  The collected attributes during the current cycle
	 * @param attributeKeys             The attribute keys used to find the monitor
	 * @return {@link Optional} instance containing the monitor
	 */
	private Optional<Monitor> findMonitor(
		final String connectorId,
		final Map<String, Monitor> sameTypeMonitors,
		final Map<String, String> collectedAttributeValues,
		final Set<String> attributeKeys
	) {
		return sameTypeMonitors
			.values()
			.stream()
			.filter(monitor ->
				matchMonitorAttributes(monitor, collectedAttributeValues, attributeKeys) &&
				connectorId.equals(monitor.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID))
			)
			.findFirst();
	}

	/**
	 * Checks if the attribute values of the given monitor match the collected attribute values.
	 *
	 * @param monitor                  The monitor instance we wish to verify its attributes
	 * @param collectedAttributeValues The collected attribute values
	 * @param attributeKeys            The attribute keys defined by the monitor job
	 * @return <code>true</code> if the monitor's attribute values identified by
	 *         the <em>attributeKeys</em> match the current collected attribute
	 *         values otherwise <code>false</code>
	 */
	private boolean matchMonitorAttributes(
		final Monitor monitor,
		final Map<String, String> collectedAttributeValues,
		final Set<String> attributeKeys
	) {
		return attributeKeys
			.stream()
			.allMatch(key -> {
				// Get the existing monitor attribute value that should be set at the discovery
				final String monitorAttributeValue = monitor.getAttribute(key);

				// The absence of the value prevents us from progressing any further
				if (monitorAttributeValue == null) {
					return false;
				}

				// Get the collected attribute value
				final String collectedAttributeValue = collectedAttributeValues.get(key);

				// The absence of the value prevents us from progressing any further
				if (collectedAttributeValue == null) {
					return false;
				}

				// Compares the existing monitor attribute value to the collected attribute value
				return monitorAttributeValue.equals(collectedAttributeValue);
			});
	}

	/**
	 *  This method is the main collection step method
	 */
	@Override
	public void run() {
		// Get the host name from telemetry manager
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Get host monitors
		final Map<String, Monitor> hostMonitors = telemetryManager.getMonitors().get(HOST.getKey());

		if (hostMonitors == null) {
			log.error("Hostname {} - No host found. Stopping collect strategy.", hostname);
			return;
		}

		// Get the endpoint host
		final Monitor host = hostMonitors
			.values()
			.stream()
			.filter(hostMonitor -> "true".equals(hostMonitor.getAttributes().get(IS_ENDPOINT)))
			.findFirst()
			.orElse(null);

		if (host == null) {
			log.error("Hostname {} - No host found. Stopping collect strategy.", hostname);
			return;
		}

		host.setDiscoveryTime(strategyTime);

		//Retrieve connector Monitor instances from TelemetryManager
		final Map<String, Monitor> connectorMonitors = telemetryManager
			.getMonitors()
			.get(KnownMonitorType.CONNECTOR.getKey());

		// Check whether the resulting map is null or empty
		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error(
				"Hostname {} - Collect - No connectors detected in the detection operation. Collect operation will now be stopped.",
				hostname
			);
			return;
		}

		//Filter connectors by their connector_id value (compiled file name) from TelemetryManager's connector store and create a list of Connector instances.
		final ConnectorStore connectorStore = telemetryManager.getConnectorStore();

		// Retrieve the detected connectors file names
		final Set<String> detectedConnectorFileNames = connectorMonitors
			.values()
			.stream()
			.map(monitor -> monitor.getAttributes().get(MONITOR_ATTRIBUTE_ID))
			.collect(Collectors.toSet());

		// Keep only detected/selected connectors, in the store they are indexed by the compiled file name
		// Build the list of the connectors
		final List<Connector> detectedConnectors = connectorStore
			.getStore()
			.entrySet()
			.stream()
			.filter(entry -> detectedConnectorFileNames.contains(entry.getKey()))
			.map(Map.Entry::getValue)
			.toList();

		// Get only connectors that define monitors
		final List<Connector> connectorsWithMonitorJobs = detectedConnectors
			.stream()
			.filter(connector -> !connector.getMonitors().isEmpty())
			.toList();

		// Sort connectors by monitor job type: first put hosts then enclosures. If two connectors have the same type of monitor job, sort them by name
		final List<Connector> sortedConnectors = connectorsWithMonitorJobs
			.stream()
			.sorted(new ConnectorMonitorTypeComparator())
			.toList();

		// Collect each connector
		sortedConnectors.forEach(connector -> collect(connector, hostname));
	}

	@Override
	public void post() {
		// Get the host name from telemetry manager
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Get host monitors
		final Map<String, Map<String, Monitor>> monitorsMap = telemetryManager.getMonitors();

		if (monitorsMap == null) {
			log.error("Hostname {} - No host found. Stopping collect strategy.", hostname);
			return;
		}
		for (Map<String, Monitor> monitors : monitorsMap.values()) {
			if (monitors != null && !monitors.isEmpty()) {
				monitors.values().stream().forEach(monitor -> refreshCollectTime(monitor));
			}
		}
	}

	/**
	 * Refresh the collect time of the {@link Monitor}'s hw.status{hw.type="<monitor-type>", state="present"} metric and set it to the current strategy time.
	 * @param monitor The {@link Monitor} to refresh
	 */
	private void refreshCollectTime(final Monitor monitor) {
		final String presentMetricName = String.format(PRESENT_STATUS, monitor.getType());

		final NumberMetric presentMetric = monitor.getMetric(presentMetricName, NumberMetric.class);

		if (presentMetric != null) {
			presentMetric.setCollectTime(strategyTime);
		} else {
			monitor.addMetric(
				presentMetricName,
				NumberMetric.builder().collectTime(strategyTime).name(presentMetricName).value(1.0).build()
			);
		}
	}
}
