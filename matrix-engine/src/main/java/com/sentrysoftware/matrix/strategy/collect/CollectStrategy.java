package com.sentrysoftware.matrix.strategy.collect;

import com.sentrysoftware.matrix.common.ConnectorMonitorTypeComparator;
import com.sentrysoftware.matrix.common.JobInfo;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.metric.MetricDefinition;
import com.sentrysoftware.matrix.connector.model.metric.MetricType;
import com.sentrysoftware.matrix.connector.model.metric.StateSet;
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
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

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

import static com.sentrysoftware.matrix.common.helpers.KnownMonitorType.HOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.IS_ENDPOINT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MAX_THREADS_COUNT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_CONNECTOR_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_ID;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_JOBS_PRIORITY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.OTHER_MONITOR_JOB_TYPES;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.THREAD_TIMEOUT;

@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectStrategy extends AbstractStrategy {
	private static final String NO_SOURCE_TABLE_CREATE_MSG = "Hostname {} - Collect - No source table created with source key {} for connector {}.";
	public CollectStrategy(
		@NonNull final TelemetryManager telemetryManager,
		final long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);

	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
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
							MONITOR_JOBS_PRIORITY.containsKey(entry.getKey()) ?
								MONITOR_JOBS_PRIORITY.get(entry.getKey()) :
								MONITOR_JOBS_PRIORITY.get(OTHER_MONITOR_JOB_TYPES)
						)
				)
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new
				));

		final Map<String, MonitorJob> sequentialMonitorJobs = connectorMonitorJobs
				.entrySet()
				.stream()
				.filter(entry -> MONITOR_JOBS_PRIORITY.containsKey(entry.getKey()))
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new
				));

		final Map<String, MonitorJob> otherMonitorJobs = connectorMonitorJobs
				.entrySet()
				.stream()
				.filter(entry -> !MONITOR_JOBS_PRIORITY.containsKey(entry.getKey()))
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					Map.Entry::getValue,
					(oldValue, newValue) -> oldValue,
					LinkedHashMap::new
				));

		// Run monitor jobs defined in monitor jobs priority map (host, enclosure, blade, disk_controller and cpu)  in sequential mode
		sequentialMonitorJobs
			.entrySet()
			.forEach(entry -> processMonitorJob(currentConnector, hostname, entry));

		// If monitor jobs execution is set to "sequential", execute monitor jobs one by one
		if (telemetryManager.getHostConfiguration().isSequential()) {

			otherMonitorJobs
				.entrySet()
				.forEach(entry -> processMonitorJob(currentConnector, hostname, entry));


		} else {
			// Execute monitor jobs in parallel
			log.info("Hostname {} - Running discovery in parallel mode. Connector: {}.", hostname, currentConnector.getConnectorIdentity()
				.getCompiledFilename());

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
		 * @param currentConnector
		 * @param hostname
		 * @param monitorJob
		 */
		private void processMonitorJob(
		final Connector currentConnector,
		final String hostname,
		final Map.Entry<String, MonitorJob> monitorJob
	) {

			if (monitorJob.getValue() instanceof StandardMonitorJob standardMonitorJob) {
				final AbstractCollect collect = standardMonitorJob.getCollect();
				//TODO collect null check

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
					.sources(
						collect.getSources(),
						collect.getExecutionOrder().stream().toList(),
						collect.getSourceDep(),
						jobInfo
					)
					.build();
				if(collect instanceof MultiInstanceCollect){
					final Map<String, Monitor> monitors = telemetryManager.findMonitorByType(monitorType);
					if(monitors == null) {
						return;
					}

					// Create the sources and the computes for a connector
					processSourcesAndComputes(
						orderedSources.getSources(),
						jobInfo
					);

					processMonitors(monitorType, collect, currentConnector, hostname, null);

				} else {
					// Get monitors by type and connectorId (connector id attr)
					final  Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorType);
					final Map<String, Monitor> sameTypeSameConnectorMonitors =  sameTypeMonitors.values()
						.stream()
						.filter(monitor -> currentConnector.getCompiledFilename().equals(monitor.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID)))
						.collect(Collectors.toMap(
							monitorEntry -> monitorEntry.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID),
							monitorEntry -> monitorEntry,
							(oldValue, newValue) -> oldValue,
							LinkedHashMap::new
						));
					// Loop on each monitor
					sameTypeSameConnectorMonitors.values()
						.stream()
						.forEach(monitor -> {
							processSourcesAndComputesWithMonitorId(
								orderedSources.getSources(),
								jobInfo,
								monitor.getAttribute(MONITOR_ATTRIBUTE_ID)
							);
							processMonitors(monitorType, collect, currentConnector, hostname, monitor);
						});
				}
			}
		}

	/**
	 * This method processes multi instances (all at once) or mono instance monitors
	 * @param monitorType type of the monitor
	 * @param collect an abstract collect
	 * @param connector a given connector
	 * @param hostname the host name
	 * @param monitor to process : null in case of multi-instance processing
	 */
		private void processMonitors(
			final String monitorType,
			final AbstractCollect collect,
			final Connector connector,
			final String hostname,
			Monitor monitor
		){

			final Mapping mapping = collect.getMapping();
			final String connectorId = connector.getCompiledFilename();
			if(mapping == null) {
				return;
			}
			final String mappingSource = mapping.getSource();

			final Optional<SourceTable> maybeSourceTable = SourceTable.lookupSourceTable(
				mappingSource,
				connectorId,
				telemetryManager
			);

			// No sourceTable no monitor
			if (maybeSourceTable.isEmpty()) {
				log.debug(NO_SOURCE_TABLE_CREATE_MSG, hostname, mappingSource,connectorId);
				return;
			}
			final List<List<String>> table = maybeSourceTable.get().getTable();

			// If we process single monitor (monoInstance), we loop until first row.
			// Otherwise, (in case of multi-instance processing), we loop over all the source table rows
			final int rowCountLimit = monitor == null ? table.size() : 1;

			for (int i= 0; i< rowCountLimit; i ++) {
				final List<String> row = table.get(i);

				// Init mapping processor
				final MappingProcessor mappingProcessor = MappingProcessor
					.builder()
					.telemetryManager(telemetryManager)
					.mapping(mapping)
					.jobInfo(JobInfo.builder().connectorName(connectorId).hostname(hostname).monitorType(monitorType).jobName("collect").build())
					.collectTime(strategyTime)
					.row(row)
					.build();

				// In case of multi-instance, this method argument "monitor" is null. So, we try to find it by type and connector
				if(monitor == null){
					// Use the mapping processor to extract attributes and resource
					final Map<String, String> noContextAttributeInterpretedValues = mappingProcessor.interpretNonContextMappingAttributes();

					final String monitorId = noContextAttributeInterpretedValues.get(MONITOR_ATTRIBUTE_ID);
					if(monitorId == null){
						continue;
					}
					final  Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorType);
					if(sameTypeMonitors == null){
						continue;
					}

					final Optional<Monitor> maybeMonitor = sameTypeMonitors.values().stream()
						.filter(currentMonitor -> monitorId.equals(currentMonitor.getAttribute(MONITOR_ATTRIBUTE_ID))
							&& connectorId.equals(currentMonitor.getAttribute(MONITOR_ATTRIBUTE_CONNECTOR_ID)))
						.findFirst();
					if(maybeMonitor.isEmpty()){
						continue;
					}
					monitor = maybeMonitor.get();
				}


				// Collect metrics
				final Map<String, String> metrics = mappingProcessor.interpretNonContextMappingMetrics();

				metrics.putAll(mappingProcessor.interpretContextMappingMetrics(monitor));

				for (final Map.Entry<String, String> metricEntry : metrics.entrySet()) {

					final String name = metricEntry.getKey();

					// Check if the conditional collection tells that the metric shouldn't be collected
					if(monitor.isMetricDeactivated(name)){
						continue;
					}
					final String value = metricEntry.getValue();

					if (value == null) {
						log.warn("Hostname {} - No value found for metric {}. Skip metric collection on {}. Connector: {}",
							hostname,
							name,
							monitorType,
							connectorId
						);
						continue;
					}

					// Get monitor metrics from connector
					final Map<String, MetricDefinition> metricDefinitionMap = connector.getMetrics();

					AbstractMetric metric = null;
					final MetricFactory metricFactory = new MetricFactory(telemetryManager);
					if (metricDefinitionMap == null) {
						metric = metricFactory.collectNumberMetric(monitor, name, value, strategyTime);
					} else {
						final MetricDefinition metricDefinition = metricDefinitionMap.get(name);

						// Check whether metric type is Enum
						if (metricDefinition == null || (metricDefinition.getType() instanceof MetricType)) {
							metric = metricFactory.collectNumberMetric(monitor, name, value, strategyTime);
						} else if (metricDefinition.getType() instanceof StateSet stateSetType) {
							// When metric type is stateSet
							final String[] stateSet = stateSetType.getSet().stream().toArray(String[]::new);
							metric = metricFactory.collectStateSetMetric(monitor, name, value, stateSet, strategyTime);
						}
					}

					// Tell the collect that the refresh time of the discovered
					// metric must be refreshed
					if (metric != null) {
						metric.setResetMetricTime(true);
					}
				}

				// Collect legacy parameters
				monitor.addLegacyParameters(mappingProcessor.interpretNonContextMappingLegacyTextParameters());
				monitor.addLegacyParameters(mappingProcessor.interpretContextMappingLegacyTextParameters(monitor));
			}
		}

	/**
	 *  This method is the main collection step method
 	 */
	@Override
	public void run() {
		// Get the host name from telemetry manager
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Get host monitors
		final Map<String, Monitor> hostMonitors = telemetryManager
				.getMonitors()
				.get(HOST.getKey());

		if (hostMonitors == null) {
			log.error("Hostname {} - No host found. Stopping discovery strategy.", hostname);
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
			log.error("Hostname {} - No host found. Stopping discovery strategy.", hostname);
			return;
		}

		host.setDiscoveryTime(strategyTime);

		//Retrieve connector Monitor instances from TelemetryManager
		final Map<String, Monitor> connectorMonitors = telemetryManager.getMonitors().get(KnownMonitorType.CONNECTOR.getKey());

		// Check whether the resulting map is null or empty
		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error("Hostname {} - Collect - No connectors detected in the detection operation. Collect operation will now be stopped.", hostname);
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
		final List<Connector> sortedConnectors = connectorsWithMonitorJobs.stream()
			.sorted(new ConnectorMonitorTypeComparator())
			.toList();

		// Discover each connector
		sortedConnectors.forEach(connector -> collect(connector, hostname));
	}

	@Override
	public void post() {
		// TODO Auto-generated method stu
	}

}
