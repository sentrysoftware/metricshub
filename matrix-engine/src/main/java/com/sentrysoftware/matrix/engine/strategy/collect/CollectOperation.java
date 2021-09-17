package com.sentrysoftware.matrix.engine.strategy.collect;

import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.CollectType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.detection.TestedConnector;
import com.sentrysoftware.matrix.engine.strategy.discovery.HardwareMonitorComparator;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring.PowerMeter;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AMBIENT_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTED_PORTS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TOTAL_BANDWIDTH_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPILED_FILE_NAME;

import static org.springframework.util.Assert.state;

@Slf4j
public class CollectOperation extends AbstractStrategy {

	private static final String NO_SOURCE_TABLE_CREATE_MSG = "Collect - No source table created with source key {} for connector {} on system {}";
	static final String NO_HW_MONITORS_FOUND_MSG = "Collect - Could not collect system {}. No hardware monitors found in the connector {}";

	@Override
	public void prepare() {
		// Reset parameters, push current value to previous value and reset initial value
		// Why ? By convention before the collect we reset the parameters except the
		// previous values in order to compute delta and rates
		strategyConfig.getHostMonitoring().resetParameters();
	}

	@Override
	public Boolean call() throws Exception {
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Collect - Start collect for system {}", hostname);

		// Get the connectors previously discovered
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final Map<String, Monitor> connectorMonitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);

		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error("Collect - No connector detected in the detection operation. Stop collect operation");
			return false;
		}

		final Set<String> detectedConnectorFileNames = connectorMonitors
				.values()
				.stream()
				.map(monitor -> monitor.getMetadata(COMPILED_FILE_NAME))
				.collect(Collectors.toSet());

		// Keep only detected/selected connectors, in the store they are indexed by the compiled file name
		// Build the list of the connectors
		final List<Connector> connectors = store
				.getConnectors()
				.entrySet()
				.stream()
				.filter(entry -> detectedConnectorFileNames.contains(entry.getKey()))
				.map(Entry::getValue)
				.collect(Collectors.toList());

		// loop over each connector then run its collect jobs
		connectors.stream()
		.filter(connector -> super.validateHardwareMonitors(connector, hostname, NO_HW_MONITORS_FOUND_MSG))
		.forEach(connector -> {

			// Get the connector monitor
			final Monitor connectorMonitor = connectorMonitors.values().stream()
			.filter(monitor -> connector.getCompiledFilename().equals(monitor.getMetadata(COMPILED_FILE_NAME)))
			.findFirst().orElseThrow();

			collect(connector, connectorMonitor, hostMonitoring, hostname);
		});

		return true;
	}

	/**
	 * Run the collect for the given connector
	 *
	 * @param connector        The connector we wish to interpret and collect
	 * @param connectorMonitor The connector monitor we wish to collect its status and testReport parameters
	 * @param hostMonitoring   The monitors container, it also wraps the {@link SourceTable} objects
	 * @param hostname         The system hostname
	 */
	void collect(final Connector connector, final Monitor connectorMonitor, final IHostMonitoring hostMonitoring,
				 final String hostname) {

		log.debug("Collect - Processing connector {} for system {}", connector.getCompiledFilename(), hostname);

		// Re-test the connector and collect the connector monitor
		collectConnectorMonitor(connector, connectorMonitor, hostname);

		// Perform collect for the hardware monitor jobs
		// The collect order is the following: Enclosure, Blade, DiskController, CPU then the rest
		connector
			.getHardwareMonitors()
			.stream()
			.sorted(new HardwareMonitorComparator())
			.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
					&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
					&& HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
			.forEach(hardwareMonitor -> collectSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostname));

		// Now collecting the rest of the monitors in parallel mode
		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connector
			.getHardwareMonitors()
			.stream()
			.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
					&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
					&& !HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
			.forEach(hardwareMonitor ->
				threadsPool.execute(() -> collectSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostname)));

		// Order the shutdown
		threadsPool.shutdown();

		try {
			// Blocks until all tasks have completed execution after a shutdown request
			threadsPool.awaitTermination(THREAD_TIMEOUT, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Waiting for threads termination aborted with an error", e);
		}
	}

	/**
	 * Run the collect for the given <code>connectorMonitor</code>
	 *
	 * @param connector        The connector we wish to test
	 * @param connectorMonitor The connector monitor we wish to collect its status and testReport parameters
	 * @param hostname         The system hostname
	 */
	void collectConnectorMonitor(final Connector connector, final Monitor connectorMonitor, final String hostname) {

		log.debug("Start Connector Monitor {} Collect.", connectorMonitor.getId());

		log.debug("Start Connector {} Test.", connector.getCompiledFilename());

		final TestedConnector testedConnector = super.testConnector(connector, hostname);

		log.debug("End of Test for Connector {}. Test Status: {}.", connector.getCompiledFilename(), getTestedConnectorStatus(testedConnector));

		final StatusParam status = super.buildStatusParamForConnector(testedConnector);
		final TextParam testReport = super.buildTestReportParameter(hostname, testedConnector);

		connectorMonitor.collectParameter(status);
		connectorMonitor.collectParameter(testReport);

		log.debug("End of the Connector Monitor {} Collect. Status: {}", connectorMonitor.getId(), status.getStatus());
	}

	/**
	 * Collect monitors of the same type. This method processes all the sources of the collect stage then collect the required monitors
	 *
	 * @param hardwareMonitor Defines the {@link Collect} valueTable, the {@link Source} to process and all the parameters
	 * @param connector       The connector we currently process
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping {@link Monitor} and {@link SourceTable} instances
	 * @param hostname        The user's configured hostname
	 */
	void collectSameTypeMonitors(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final String hostname) {

		// Process all the sources with theirs computes
		if (CollectType.MULTI_INSTANCE.equals(hardwareMonitor.getCollect().getType())) {

			collectMultiInstance(hardwareMonitor,
					connector,
					hostMonitoring,
					hardwareMonitor.getType(),
					hardwareMonitor.getCollect().getParameters(),
					hardwareMonitor.getCollect().getSources(),
					hostname);

		} else {
			collectMonoInstance(hardwareMonitor,
					connector,
					hostMonitoring,
					hardwareMonitor.getType(),
					hardwareMonitor.getCollect().getParameters(),
					hardwareMonitor.getCollect().getSources(),
					hostname);
		}

	}

	/**
	 * Perform a MultiInstance collect. Process sources and computes then process the value table to collect monitors.
	 *
	 * @param hardwareMonitor Defines the {@link Collect} valueTable, the {@link Source} to process and all the mapping
	 * @param connector       The connector we currently process
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping {@link Monitor} and {@link SourceTable} instances
	 * @param monitorType     The type of the monitor e.g. ENCLOSURE
	 * @param parameters      The mapping, i.e. parameter name to column index
	 * @param sources         The connector {@link Source} beans to process
	 * @param hostname        The system hostname used for debug purpose
	 */
	void collectMultiInstance(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final MonitorType monitorType,
			final Map<String, String> parameters, final List<Source> sources,
			final String hostname) {

		// Run the sources and the computes
		processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname);

		// Process the MultiInstance value table
		processMultiInstanceValueTable(hardwareMonitor.getCollect().getValueTable(),
				connector.getCompiledFilename(),
				hostMonitoring,
				parameters,
				monitorType,
				hostname);
	}

	/**
	 * Collect monitors from the given valueTable, for each row we collect the {@link Monitor} instance then we set the parameters on each
	 * existing monitor
	 *
	 * @param valueTable     The unique key of the {@link Source} used to collect metrics
	 * @param connectorName  The unique name of the {@link Connector}. The compiled file name
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping source tables and monitors
	 * @param parameters     The collect parameters to process (from the connector)
	 * @param monitorType    The current type of the monitor, {@link MonitorType}
	 * @param hostname       The user's configured hostname used for debug purpose
	 */
	void processMultiInstanceValueTable(final String valueTable, final String connectorName,
			final IHostMonitoring hostMonitoring, final Map<String, String> parameters,
			final MonitorType monitorType, final String hostname) {

		// Get the source table used to collect parameters
		final SourceTable sourceTable = hostMonitoring
				.getConnectorNamespace(connectorName)
				.getSourceTable(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.debug(NO_SOURCE_TABLE_CREATE_MSG,
					valueTable, connectorName, hostname);
			return;
		}

		// Loop over each row (List) and collect the corresponding monitor
		collectMonitors(valueTable,
				sourceTable,
				connectorName,
				hostMonitoring,
				parameters,
				monitorType,
				hostname);
	}

	/**
	 * Loop over the given {@link SourceTable} and collect each monitor identified with {@link HardwareConstants#DEVICE_ID}
	 *
	 * @param valueTable     The unique key of the {@link Source} used to collect metrics
	 * @param connectorName  The unique name of the {@link Connector}. The compiled file name
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping all the monitors
	 * @param parameters     The collect parameters to process (mapping from the connector)
	 * @param monitorType    The current type of the monitor, {@link MonitorType}
	 * @param hostname       The user's configured hostname used for debug purpose
	 * @param sourceTable    The collected information formatted in a {@link SourceTable} object
	 */
	void collectMonitors(final String valueTable, final SourceTable sourceTable,
			final String connectorName, final IHostMonitoring hostMonitoring,
			final Map<String, String> parameters, final MonitorType monitorType,
			final String hostname) {

		for (final List<String> row : sourceTable.getTable()) {

			Optional<Monitor> monitorOpt = getMonitor(valueTable,
					monitorType,
					hostMonitoring,
					row,
					parameters.get(DEVICE_ID));

			if (monitorOpt.isEmpty()) {
				log.warn("Collect - Couldn't find monitor {} associated with row {}. Connector {}",
						monitorType.getNameInConnector(), row, connectorName);
				continue;
			}

			log.debug("Collecting monitor id {}", monitorOpt.get().getId());

			// Build the collect information as the parameters are collected by the MonitorCollectVisitor
			// so that we avoid the tightly coupling with the current CollectOperation strategy.
			final MonitorCollectInfo monitorCollectInfo = MonitorCollectInfo
					.builder()
					.connectorName(connectorName)
					.hostMonitoring(hostMonitoring)
					.hostname(hostname)
					.row(row)
					.mapping(parameters)
					.monitor(monitorOpt.get())
					.valueTable(valueTable)
					.collectTime(strategyTime)
					.unknownStatus(strategyConfig.getEngineConfiguration().getUnknownStatus())
					.build();

			// Here we go...
			monitorType.getMetaMonitor().accept(new MonitorCollectVisitor(monitorCollectInfo));

		}
	}

	/**
	 * Get the monitor to collect using the given row and the parameters mapping.
	 *
	 * @param valueTable               The unique key of the {@link Source} used for debug purpose
	 * @param monitorType              The type of the monitor we wish to collect
	 * @param hostMonitoring           The {@link IHostMonitoring} instance wrapping source tables and monitors
	 * @param row                      The data which indicate the device id used to find the monitor from the {@link HostMonitoring}
	 * @param deviceIdValueTableColumn The column index formatted as `ValueTable.Column($number)`
	 * @return {@link Optional} {@link Monitor} instance
	 */
	Optional<Monitor> getMonitor(final String valueTable, final MonitorType monitorType,
			final IHostMonitoring hostMonitoring, final List<String> row,
			final String deviceIdValueTableColumn) {

		if (deviceIdValueTableColumn == null) {
			return Optional.empty();
		}

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(monitorType);

		if (monitors == null) {
			return Optional.empty();
		}

		final String id = CollectHelper.getValueTableColumnValue(valueTable,
				DEVICE_ID,
				monitorType,
				row,
				deviceIdValueTableColumn);

		if (id != null) {
			return monitors.values().stream()
					.filter(mo -> Objects.nonNull(mo.getMetadata()) &&
							id.equals(mo.getMetadata().get(DEVICE_ID)))
					.findFirst();
		}

		return Optional.empty();
	}

	/**
	 * Perform a MonoInstance collect. Process sources and computes then process the value table per monitor
	 *
	 * @param hardwareMonitor Defines the {@link Collect} valueTable, the {@link Source} to process and all the mapping
	 * @param connector       The connector we currently process
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping {@link Monitor} and {@link SourceTable} instances
	 * @param monitorType     The type of the monitor e.g. ENCLOSURE
	 * @param parameters      The mapping, i.e. parameter name to column index
	 * @param sources         The connector {@link Source} beans to process
	 * @param hostname        The system hostname used for debug purpose
	 */
	void collectMonoInstance(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final MonitorType monitorType,
			final Map<String, String> parameters, final List<Source> sources,
			final String hostname) {

		// Get the same type monitors from the hostMonitoring
		// keep only the monitors of the current connector
		final List<Monitor> monitors = getSameTypeSameConnectorMonitors(hardwareMonitor.getType(), connector.getCompiledFilename(), hostMonitoring);

		monitors.forEach(monitor -> {

			log.debug("Collecting monitor id {}", monitor.getId());

			// Process sources and computes
			processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname, monitor);

			// Process value table
			processMonoInstanceValueTable(monitor,
					hardwareMonitor.getCollect().getValueTable(),
					connector.getCompiledFilename(),
					hostMonitoring,
					parameters,
					monitorType,
					hostname);

		});
	}

	/**
	 * Get the monitors with the same type and discovered from the connector identified by the given <code>connectorName</code>
	 *
	 * @param monitorType    The type of the monitor used to fetch monitors from the {@link HostMonitoring}
	 * @param connectorName  The unique name of the {@link Connector}
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping {@link Monitor} instances
	 * @return {@link List} of {@link Monitor} instances
	 */
	List<Monitor> getSameTypeSameConnectorMonitors(final MonitorType monitorType, final String connectorName,
			final IHostMonitoring hostMonitoring) {

		Map<String, Monitor> sameTypeMonitors = hostMonitoring.selectFromType(monitorType);
		if (sameTypeMonitors == null) {
			return Collections.emptyList();
		}

		return sameTypeMonitors
			.values()
			.stream()
			.filter(monitor -> Objects.nonNull(monitor.getMetadata())
						&& connectorName.equals(monitor.getMetadata().get(CONNECTOR)))
			.collect(Collectors.toList());
	}

	/**
	 * Collect the {@link Monitor} instance from the given valueTable.<br>
	 * This method extracts the first row from the {@link SourceTable}, then collects the {@link Monitor} instance to set the parameters on
	 * the monitor instance via the {@link MonitorCollectVisitor}
	 *
	 * @param monitor        The monitor we wish to collect
	 * @param valueTable     The unique key of the {@link Source} used to collect metrics
	 * @param connectorName  The unique name of the {@link Connector}. The compiled file name
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping source tables and monitors
	 * @param parameters     The collect parameters to process (from the connector)
	 * @param monitorType    The current type of the monitor, {@link MonitorType}
	 * @param hostname       The user's configured hostname used for debug purpose
	 */
	void processMonoInstanceValueTable(final Monitor monitor, final String valueTable, final String connectorName,
			final IHostMonitoring hostMonitoring, final Map<String, String> parameters,
			final MonitorType monitorType, final String hostname) {

		PresentParam presentParam = monitor.getParameter(PRESENT_PARAMETER, PresentParam.class);
		if (presentParam != null
				&& presentParam.getPresent() != null
				&& presentParam.getPresent() == 0) {
			return;
		}

		// Get the source table used to collect parameters
		final SourceTable sourceTable = hostMonitoring
				.getConnectorNamespace(connectorName)
				.getSourceTable(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.debug(NO_SOURCE_TABLE_CREATE_MSG, valueTable, connectorName, hostname);
			return;
		}

		// Make sure the table is not empty
		if (sourceTable.getTable().isEmpty()) {
			log.error("Collect - Empty source table created with source key {} for connector {} on system {}",
					valueTable, connectorName, hostname);
			return;
		}

		// Build the collect information as the parameters are collected by the MonitorCollectVisitor
		// so that we avoid the tightly coupling with the current CollectOperation strategy.
		final MonitorCollectInfo monitorCollectInfo = MonitorCollectInfo
				.builder()
				.connectorName(connectorName)
				.hostMonitoring(hostMonitoring)
				.hostname(hostname)
				.row(sourceTable.getTable().get(0))
				.mapping(parameters)
				.monitor(monitor)
				.valueTable(valueTable)
				.collectTime(strategyTime)
				.unknownStatus(strategyConfig.getEngineConfiguration().getUnknownStatus())
				.build();

		// Here we go...
		monitorType.getMetaMonitor().accept(new MonitorCollectVisitor(monitorCollectInfo));
	}


	/**
	 * Return <code>true</code> if the following conditions are met
	 * <ol>
	 *     <li>The MonitorType of the given hardwareMonitor is not null</li>
	 *     <li>The Collect of the given hardwareMonitor is not null</li>
	 *     <li>The CollectType of the given hardwareMonitor is not null</li>
	 *     <li>The parameters map of the given hardwareMonitor is not null or empty</li>
	 *     <li>The valueTable of the collect of the given hardwareMonitor is not null</li>
	 * </ol>
	 * This methods outputs a warning message when one of the above conditions is not met<br>
	 * <br>
	 *
	 * @param hardwareMonitor The {@link HardwareMonitor} we wish to validate its fields
	 * @param connectorName   The name of the connector used for debug purpose
	 * @param hostname        The system hostname used for debug purpose
	 * @return boolean value
	 */
	boolean validateHardwareMonitorFields(final HardwareMonitor hardwareMonitor, final String connectorName, final String hostname) {

		final MonitorType monitorType = hardwareMonitor.getType();
		if (monitorType == null) {
			log.warn("Collect - No type specified for hardware monitor job with connector {} on system {}",
					connectorName, hostname);
			return false;
		}

		if (hardwareMonitor.getCollect() == null) {
			log.warn("Collect - No {} monitor job specified during the collect for the connector {} on system {}",
					monitorType.getNameInConnector(), connectorName, hostname);
			return false;
		}

		// Check the collectType
		if (hardwareMonitor.getCollect().getType() == null) {
			log.warn("Collect - No collect type found with {} during the collect for the connector {} on system {}",
					monitorType.getNameInConnector(), connectorName, hostname);
			return false;
		}

		// Check the collect parameters, so later in the code we can create the monitor with the metadata
		final Map<String, String> parameters = hardwareMonitor.getCollect().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("Collect - No parameter found with {} during the collect for the connector {} on system {}",
					monitorType.getNameInConnector(), connectorName, hostname);
			return false;
		}

		// Check the valueTable key
		if (hardwareMonitor.getCollect().getValueTable() == null) {
			log.error("Collect - No valueTable found with monitor {} for connector {} on system {}",
					monitorType.getNameInConnector(), connectorName, hostname);
			return false;
		}

		return true;
	}

	/**
	 * Refresh the collect time of the {@link PresentParam} in the given {@link Monitor} instance.
	 *
	 * @param monitor		The {@link Monitor} whose {@link PresentParam}'s collect time should be refreshed.
	 * @param collectTime	The new collect time.
	 */
	static void refreshPresentCollectTime(final Monitor monitor, final Long collectTime) {
		final PresentParam presentParam = monitor.getParameter(PRESENT_PARAMETER, PresentParam.class);
		if (presentParam != null) {
			presentParam.setCollectTime(collectTime);
		}
	}

	/**
	 * Set the collect time in all the present parameters
	 */
	private void refreshPresentParameters() {
		strategyConfig.getHostMonitoring()
		.getMonitors()
		.values()
		.stream()
		.map(Map::values)
		.flatMap(Collection::stream)
		.filter(monitor -> monitor.getMonitorType().getMetaMonitor().hasPresentParameter())
		.forEach(monitor -> refreshPresentCollectTime(monitor, strategyTime));
	}

	/**
	 * @param array1	The first array. Cannot be null.
	 * @param array2	The second array. Cannot be null. Must be the same size as <em>array1</em>.
	 *
	 * @return			A new array with element at index i being the sum of <em>array1[i]</em> and <em>array2[i]</em>.
	 * 					<br>If any of <em>array1[i]</em> and <em>array2[i]</em> is null, then the resulting sum is null.
	 */
	private Double[] sumArrayValues(Double[] array1, Double[] array2) {

		Double[] result = new Double[array1.length];

		for (int i = 0; i < array1.length; i++) {

			result[i] = array1[i] != null && array2[i] != null
				? array1[i] + array2[i]
				: null;
		}

		return result;
	}

	/**
	 * Setting the target energy value as the sum of all the {@link Enclosure}s' energy values.
	 */
	private void aggregateTargetEnergy() {

		IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		state(hostMonitoring != null, "hostMonitoring should not be null.");

		// Getting the target monitor
		Monitor targetMonitor = getTargetMonitor(hostMonitoring);

		// Getting the enclosure monitors
		Map<String, Monitor> enclosureMonitors = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		if (enclosureMonitors == null || enclosureMonitors.isEmpty()) {
			return;
		}

		// Getting the sums of the enclosures' energy converted values and raw values
		// totalEnergyValues[0] is the total converted value
		// totalEnergyValues[1] is the total raw value
		Double[] totalEnergyValues = enclosureMonitors
			.values()
			.stream()
			.map(monitor -> monitor.getParameter(ENERGY_PARAMETER, NumberParam.class))
			.filter(Objects::nonNull)
			.map(numberParam -> new Double[] {numberParam.getValue(), numberParam.getRawValue()})
			.reduce(this::sumArrayValues)
			.orElse(null);

		if (totalEnergyValues == null || totalEnergyValues[0] == null || totalEnergyValues[1] == null) {

			// totalEnergyValues[0] != null and totalEnergyValues[1] == null should never happen...

			return;
		}

		// Building the parameter
		NumberParam targetEnergy = NumberParam
			.builder()
			.name(ENERGY_PARAMETER)
			.unit(ENERGY_PARAMETER_UNIT)
			.collectTime(strategyTime)
			.value(totalEnergyValues[0])
			.rawValue(totalEnergyValues[1])
			.build();

		// Adding the parameter to the target monitor
		targetMonitor.collectParameter(targetEnergy);
	}

	/**
	 * @param metadata		The {@link Monitor}'s metadata.
	 * @param temperature	The {@link Temperature} parameter.
	 *
	 * @return				The difference between the {@link Monitor}'s temperature threshold
	 * 						and the given {@link Temperature}'s value.
	 */
	private Double computeTemperatureHeatingMargin(Map<String, String> metadata, NumberParam temperature) {

		final Double warningThresholdValue = getTemperatureWarningThreshold(metadata);

		final Double temperatureValue = temperature.getValue();

		return (temperatureValue != null && warningThresholdValue != null) ? warningThresholdValue - temperatureValue : null;
	}

	/**
	 * Setting the target heating margin value as the minimum value of all the {@link Temperature}s' heating margins.
	 */
	private void computeTargetHeatingMargin() {

		IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		state(hostMonitoring != null, "hostMonitoring should not be null.");

		// Getting the target monitor
		Monitor targetMonitor = getTargetMonitor(hostMonitoring);

		// Getting the temperature monitors
		Map<String, Monitor> temperatureMonitors = hostMonitoring.selectFromType(MonitorType.TEMPERATURE);
		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			return;
		}

		// Getting the minimum heating margin among all the temperatures' heating margin values
		Double minimumHeatingMargin = temperatureMonitors
			.values()
			.stream()
			.filter(monitor -> monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class) != null)
			.map(monitor -> computeTemperatureHeatingMargin(monitor.getMetadata(),
				monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class)))
			.filter(Objects::nonNull)
			.reduce(Double::min)
			.orElse(null);

		if (minimumHeatingMargin == null) {
			return;
		}

		// Building the parameter
		NumberParam targetHeatingMargin = NumberParam
			.builder()
			.name(HEATING_MARGIN_PARAMETER)
			.unit(HEATING_MARGIN_PARAMETER_UNIT)
			.collectTime(strategyTime)
			.value(minimumHeatingMargin)
			.rawValue(minimumHeatingMargin)
			.build();

		// Adding the parameter to the target monitor
		targetMonitor.collectParameter(targetHeatingMargin);
	}

	@Override
	public void release() {
		// Not implemented yet
	}

	/**
	 * Compute temperature parameters for the current target monitor:
	 * <ul>
	 * <li><b>ambientTemperature</b>: the minimum temperature between 5 and 100 degrees Celsius</li>
	 * <li><b>cpuTemperature</b>: the average CPU temperatures</li>
	 * <li><b>cpuThermalDissipationRate</b>: the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit)</li>
	 * </ul>
	 */
	void computeTargetTemperatureParameters() {
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> temperatureMonitors = hostMonitoring
				.selectFromType(MonitorType.TEMPERATURE);

		final Monitor targetMonitor = getTargetMonitor(hostMonitoring);

		// No temperatures then no computation
		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			log.debug(
					"Could not compute temperature parameters (ambientTemperature, cpuTemperature, cpuThermalDissipationRate) on the given host: {}",
					strategyConfig.getEngineConfiguration().getTarget().getHostname());
			return;
		}

		double ambientTemperature = 100.0;
		double cpuTemperatureAverage = 0;
		double cpuTemperatureCount = 0;

		// Loop over all the temperature monitors to compute the ambient temperature, cpuTemperatureCount and cpuTemperatureAverage
		for (final Monitor temperatureMonitor : temperatureMonitors.values()) {

			// Get the temperature value
			final Double temperatureValue = CollectHelper.getNumberParamValue(temperatureMonitor, TEMPERATURE_PARAMETER);

			// If there is not temperature value, no need to continue process this monitor.
			if (temperatureValue == null) {
				continue;
			}

			// Is this the ambient temperature? (which should be the lowest measured temperature... except if it's less than 5°)
			if (temperatureValue < ambientTemperature && temperatureValue > 5) {
				ambientTemperature = temperatureValue;
			}

			// Get the isCpuSensor flag
			final boolean isCpuSensor = Boolean.parseBoolean(temperatureMonitor.getMetadata(IS_CPU_SENSOR));

			// Is this a CPU sensor?
			if (isCpuSensor && temperatureValue > 5) {
				cpuTemperatureAverage += temperatureValue;
				cpuTemperatureCount++;
			}
		}

		// Sets the host ambient temperature as the minimum of all temperature sensors
		if (ambientTemperature < 100) {

			// Update the parameter
			CollectHelper.updateNumberParameter(
				targetMonitor,
				AMBIENT_TEMPERATURE_PARAMETER,
				TEMPERATURE_PARAMETER_UNIT,
				strategyTime,
				ambientTemperature,
				ambientTemperature
			);

		}

		// Sets the average CPU temperature (to estimate the heat dissipation of the processors)
		if (cpuTemperatureCount > 0) {

			// Compute the average
			cpuTemperatureAverage /= cpuTemperatureCount;

			cpuTemperatureAverage = NumberHelper.round(cpuTemperatureAverage, 2, RoundingMode.HALF_UP);

			// Update the parameter
			CollectHelper.updateNumberParameter(
				targetMonitor,
				CPU_TEMPERATURE_PARAMETER,
				TEMPERATURE_PARAMETER_UNIT,
				strategyTime,
				cpuTemperatureAverage,
				cpuTemperatureAverage
			);

			// Calculate the dissipation rate
			computeTargetThermalDissipationRate(targetMonitor, ambientTemperature, cpuTemperatureAverage);
		}

	}

	/**
	 * Calculate the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit).
	 *
	 * @param targetMonitor         The target monitor we wish to update its heat dissipation rate
	 * @param ambientTemperature    The ambient temperature of the host
	 * @param cpuTemperatureAverage The CPU average temperature previously computed based on the cpu sensor count
	 */
	void computeTargetThermalDissipationRate(final Monitor targetMonitor, final double ambientTemperature, final double cpuTemperatureAverage) {

		// Get the average CPU temperature computed at the discovery level
		final double ambientToWarningDifference = NumberHelper.parseDouble(
				targetMonitor.getMetadata(AVERAGE_CPU_TEMPERATURE_WARNING), 0.0) - ambientTemperature;

		// Avoid the arithmetic exception
		if (ambientToWarningDifference != 0.0) {
			double cpuThermalDissipationRate = (cpuTemperatureAverage - ambientTemperature) / ambientToWarningDifference;

			// Do we have a consistent fraction
			if (cpuThermalDissipationRate >= 0 && cpuThermalDissipationRate <= 1) {

				cpuThermalDissipationRate = NumberHelper.round(cpuThermalDissipationRate, 2, RoundingMode.HALF_UP);

				CollectHelper.updateNumberParameter(
					targetMonitor,
					CPU_THERMAL_DISSIPATION_RATE_PARAMETER,
					"",
					strategyTime,
					cpuThermalDissipationRate,
					cpuThermalDissipationRate
				);
			}
		}
	}

	/**
	 * Compute network card parameters for the current target monitor:
	 * <ul>
	 * <li><b>connectedPortsCount</b>: the number of connected network ports</li>
	 * <li><b>totalBandwidth</b>: the total bandwidth available across all network cards</li>
	 * </ul>
	 */
	void computeNetworkCardParameters() {
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final Map<String, Monitor> networkCardMonitors = hostMonitoring.selectFromType(MonitorType.NETWORK_CARD);
		if (networkCardMonitors == null || networkCardMonitors.isEmpty()) {
			return;
		}

		double connectedPortsCount = 0;
		double totalBandwidth = 0;

		// Loop over all the network card monitors to compute the totalBandwidth & connectedPortsCount
		for (final Monitor networkCardMonitor : networkCardMonitors.values()) {

			// Get the link status
			final ParameterState linkStatus = CollectHelper.getStatusParamState(networkCardMonitor, LINK_STATUS_PARAMETER);

			// If there is connected count it.
			if (linkStatus != null && ParameterState.OK.equals(linkStatus)) {
				connectedPortsCount++;
			}

			// Get the link speed value
			final Double linkSpeed = CollectHelper.getNumberParamValue(networkCardMonitor, LINK_SPEED_PARAMETER);

			// If there is a speed add it.
			if (linkSpeed != null) {
				totalBandwidth += linkSpeed;
			}
		}

		// Create the parameter for connectedPortsCount
		final NumberParam connectedPortsCountParam = NumberParam.builder()
				.name(CONNECTED_PORTS_COUNT_PARAMETER)
				.unit(CONNECTED_PORTS_PARAMETER_UNIT)
				.collectTime(strategyTime)
				.value(connectedPortsCount)
				.rawValue(connectedPortsCount)
				.build();

		// Create the parameter for totalBandwidth
		final NumberParam totalBandwidthParam = NumberParam.builder()
				.name(TOTAL_BANDWIDTH_PARAMETER)
				.unit(SPEED_MBITS_PARAMETER_UNIT)
				.collectTime(strategyTime)
				.value(totalBandwidth)
				.rawValue(totalBandwidth)
				.build();

		// Add the new parameters to the target monitor
		final Monitor targetMonitor = getTargetMonitor(hostMonitoring);
		targetMonitor.collectParameter(connectedPortsCountParam);
		targetMonitor.collectParameter(totalBandwidthParam);
	}

	@Override
	public void post() {

		// Refresh present parameters
		refreshPresentParameters();

		// Setting the target total energy
		aggregateTargetEnergy();

		// Setting the target heating margin
		computeTargetHeatingMargin();

		// Compute temperatures
		computeTargetTemperatureParameters();

		// Estimate CPUs Power Consumption
		// The CPUs power consumption needs to be estimated in the post collect strategy
		// because the computation requires the target Thermal Dissipation Rate which is also collected at the end of the collect.
		estimateCpusPowerConsumption();

		// Estimate the target Power Consumption
		// The target estimated power consumption is the sum of all monitor's power consumption that are not missing (Present = 1) divided by 0.9, to
		// account for the power supplies' heat dissipation (90% efficiency assumed).
		estimateTargetPowerConsumption();
	}

	/**
	 * Estimate the target power consumption.<br> Perform the the sum of all monitor's power consumption, energy and energy usage, excluding missing
	 * monitors. The final value is divided by 0.9 to add 10% to the final value so that we account the power supplies' heat dissipation (90%
	 * efficiency assumed)
	 */
	void estimateTargetPowerConsumption() {
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		// Getting the target monitor
		final Monitor targetMonitor = getTargetMonitor(hostMonitoring);

		// Get the target energy
		final Double targetEnergy = CollectHelper.getNumberParamValue(targetMonitor, ENERGY_PARAMETER);
		if (targetEnergy != null) {
			log.debug("The energy has been collected for system: {}. Value: {} Joules. Power Meter is now collected.",
					hostname, targetEnergy);
			hostMonitoring.setPowerMeter(PowerMeter.COLLECTED);
			return;
		}

		final Map<String, Monitor> enclosureMonitors = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);
		// The connector has collected PowerConsumption on the Enclosure monitors so we don't need to estimate the power on the target
		// The energy will be collected during the next collect when the calculation is from the power consumption
		// If the connector has collected Energy using the connector then we will never reach this part of code because the previous targetEnergy
		// will never be null
		if (enclosureMonitors != null && enclosureMonitors.values().stream()
				.anyMatch(monitor -> CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER) != null)) {
			log.debug("The energy is going to be collected during the next collect for system {}.", hostname);
			return;
		}

		// Browse through all the collected objects and perform the sum of parameters using the map-reduce
		final Double[] totalValues = hostMonitoring.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> !monitor.isMissing()) // Skip missing
			.filter(monitor -> !MonitorType.ENCLOSURE.equals(monitor.getMonitorType())) // Skip the enclosure
			.filter(monitor -> CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER) != null) // skip monitors without power consumption
			.map(monitor -> new Double[] {
						CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER),
						CollectHelper.getNumberParamValue(monitor, ENERGY_USAGE_PARAMETER),
						CollectHelper.getNumberParamValue(monitor, ENERGY_PARAMETER)})
			.reduce(this::sumArrayValues)
			.orElse(null);

		if (totalValues == null) {
			log.debug("No power consumption estimated for the monitored devices on system {}.", hostname);
			return;
		}

		// Set power meter to estimated
		hostMonitoring.setPowerMeter(PowerMeter.ESTIMATED);

		// totalValues[0] can never be null as we have already filtered power consumption null values
		// Add 10% because of the heat dissipation of the power supplies
		final double powerConsumption = NumberHelper.round(totalValues[0] / 0.9, 2, RoundingMode.HALF_UP);
		if (powerConsumption > 0) {
			CollectHelper.updateNumberParameter(
				targetMonitor,
				POWER_CONSUMPTION_PARAMETER,
				POWER_CONSUMPTION_PARAMETER_UNIT,
				strategyTime,
				powerConsumption,
				powerConsumption
			);
			log.debug("Power Consumption: Estimated at {} Watts on system {}.", powerConsumption, hostname);

		} else {
			log.debug("Power Consumption could not be estimated on system {}.", hostname);
		}

		// Do we have the energy usage value, the first collect will always return null for the energy usage
		// as we didn't get the delta time to calculate the energy usage delta
		if (totalValues[1] != null) {
			final double energyUsage =  NumberHelper.round(totalValues[1] / 0.9, 2, RoundingMode.HALF_UP);
			if (energyUsage > 0) {
				CollectHelper.updateNumberParameter(
					targetMonitor,
					ENERGY_USAGE_PARAMETER,
					ENERGY_USAGE_PARAMETER_UNIT,
					strategyTime,
					energyUsage,
					energyUsage
				);
				log.debug("Energy Usage: Estimated at {} Joules on system {}.", energyUsage, hostname);
			} else {
				log.debug("Energy Usage could not be estimated on system {}.", hostname);
			}

		}

		// Do we have the energy value, the first collect will always return null for the energy
		// as we didn't get the delta time to calculate the energy usage delta
		if (totalValues[2] != null) {
			final double energy =  NumberHelper.round(totalValues[2] / 0.9, 2, RoundingMode.HALF_UP);
			if (energy > 0) {
				CollectHelper.updateNumberParameter(
					targetMonitor,
					ENERGY_PARAMETER,
					ENERGY_PARAMETER_UNIT,
					strategyTime,
					energy,
					energy
				);
				log.debug("Energy: Estimated at {} Joules on system {}", energy, hostname);
			} else {
				log.debug("Energy could not be estimated on system {}.", hostname);
			}

		}
	}

	/**
	 * Estimates the power consumption of the processors
	 */
	void estimateCpusPowerConsumption() {
		final String hostname = strategyConfig
				.getEngineConfiguration()
				.getTarget().getHostname();
		final Long collectTime = this.strategyTime;
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> cpus = hostMonitoring.selectFromType(MonitorType.CPU);

		if (cpus == null) {
			log.debug("No CPU discovered for system {}. Skip CPUs Power Consumption estimation.", hostname);
			return;
		}

		final Monitor target = getTargetMonitor(hostMonitoring);

		cpus.values()
			.stream()
			.filter(cpu -> !cpu.isMissing())
			.forEach(cpu -> estimateCpuPowerConsumption(cpu, target, collectTime, hostname));
	}

	/**
	 * Estimates the power dissipation of a processor, based on some characteristics Inspiration:
	 * https://en.wikipedia.org/wiki/List_of_CPU_power_dissipation_figures Page 11 of
	 * http://www.cse.iitd.ernet.in/~srsarangi/files/papers/powersurvey.pdf
	 *
	 * @param cpu         The CPU monitor we wish to estimate its power dissipation
	 * @param target      The target root parent monitor from which we extract the overall dissipation rate of the processors
	 * @param collectTime The current strategy collect time
	 * @param hostname    The system hostname
	 */
	void estimateCpuPowerConsumption(@NonNull final Monitor cpu, @NonNull final Monitor target,
			@NonNull final Long collectTime, @NonNull String hostname) {

		Double maximumPowerConsumption = NumberHelper.parseDouble(cpu.getMetadata(POWER_CONSUMPTION), null);

		// If we didn't get the actual maximum power consumption, discovered by the DiscoveryOperation strategy, assume it's 19W/GHz
		if (maximumPowerConsumption == null) {

			// Get the maximum speed, discovered metadata.
			double maximumSpeed = NumberHelper.parseDouble(cpu.getMetadata(MAXIMUM_SPEED), 0.0);

			// No processor speed? Assume 2.5GHz for the calculation
			if (maximumSpeed <= 0) {
				maximumSpeed = 2500.0;
			}

			maximumPowerConsumption = maximumSpeed / 1000 * 19.0;
		}

		// Get the thermal dissipation rate collected on the target monitor at the end of the collect
		Double thermalDissipationRate = CollectHelper.getNumberParamValue(target, CPU_THERMAL_DISSIPATION_RATE_PARAMETER);

		// If we didn't have a thermal dissipation rate value, then assume it's at 25%
		if (thermalDissipationRate == null) {
			thermalDissipationRate = 0.25;
		}

		// Compute the estimated power consumption
		final double powerConsumption = NumberHelper.round(maximumPowerConsumption * thermalDissipationRate, 2, RoundingMode.HALF_UP);

		// This will set the energy, the delta energy called energyUsage and the powerConsumption on the cpu monitor
		CollectHelper.collectEnergyUsageFromPower(cpu, collectTime, powerConsumption, hostname);
	}

}
