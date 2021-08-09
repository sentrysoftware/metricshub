package com.sentrysoftware.matrix.engine.strategy.collect;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
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

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_THRESHOLD;
import static org.springframework.util.Assert.notNull;
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

		final Set<String> detectedConnectorNames = connectorMonitors
				.values()
				.stream()
				.map(Monitor::getName)
				.collect(Collectors.toSet());

		// Build the list of the connectors
		final List<Connector> connectors = store
				.getConnectors()
				.entrySet()
				.stream()
				.filter(entry -> detectedConnectorNames.contains(entry.getKey()))
				.map(Entry::getValue)
				.collect(Collectors.toList());

		// loop over each connector then run its collect jobs
		connectors.stream()
		.filter(connector -> super.validateHardwareMonitors(connector, hostname, NO_HW_MONITORS_FOUND_MSG))
		.forEach(connector -> {

			// Get the connector monitor
			final Monitor connectorMonitor = connectorMonitors.values().stream()
			.filter(monitor -> connector.getCompiledFilename().equals(monitor.getName()))
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

		// Now collecting the rest of the monitors in parallel mode
		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connector
			.getHardwareMonitors()
			.stream()
			.filter(hardwareMonitor ->
				validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname))
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

		connectorMonitor.addParameter(status);
		connectorMonitor.addParameter(testReport);

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
		final SourceTable sourceTable = hostMonitoring.getSourceTableByKey(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.error(NO_SOURCE_TABLE_CREATE_MSG,
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
					parameters.get(HardwareConstants.DEVICE_ID));

			if (monitorOpt.isEmpty()) {
				log.warn("Collect - Couldn't find monitor {} associated with row {}. Connector {}",
						monitorType.getName(), row, connectorName);
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
				HardwareConstants.DEVICE_ID,
				monitorType,
				row,
				deviceIdValueTableColumn);

		if (id != null) {
			return monitors.values().stream()
					.filter(mo -> Objects.nonNull(mo.getMetadata()) &&
							id.equals(mo.getMetadata().get(HardwareConstants.DEVICE_ID)))
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
		return hostMonitoring.selectFromType(monitorType).values().stream()
				.filter(monitor -> Objects.nonNull(monitor.getMetadata())
						&& connectorName.equals(monitor.getMetadata().get(HardwareConstants.CONNECTOR)))
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
		final SourceTable sourceTable = hostMonitoring.getSourceTableByKey(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.error(NO_SOURCE_TABLE_CREATE_MSG, valueTable, connectorName, hostname);
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
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		// Check the collectType
		if (hardwareMonitor.getCollect().getType() == null) {
			log.warn("Collect - No collect type found with {} during the collect for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		// Check the collect parameters, so later in the code we can create the monitor with the metadata
		final Map<String, String> parameters = hardwareMonitor.getCollect().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("Collect - No parameter found with {} during the collect for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		// Check the valueTable key
		if (hardwareMonitor.getCollect().getValueTable() == null) {
			log.error("Collect - No valueTable found with monitor {} for connector {} on system {}", 
					monitorType.getName(), connectorName, hostname);
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
		final PresentParam presentParam = monitor.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class);
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
	 * @param hostMonitoring The {@link IHostMonitoring} instance.
	 *
	 * @return	The target {@link Monitor} in the given {@link IHostMonitoring} instance.
	 */
	private Monitor getTargetMonitor(IHostMonitoring hostMonitoring) {

		Map<String, Monitor> targetMonitors = hostMonitoring.selectFromType(MonitorType.TARGET);
		state(targetMonitors != null && !targetMonitors.isEmpty(), "targetMonitors should not be null or empty.");

		return targetMonitors
			.values()
			.stream()
			.findFirst()
			.orElseThrow();
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
		targetMonitor.addParameter(targetEnergy);
	}

	/**
	 * @param metadata		The {@link Monitor}'s metadata.
	 * @param temperature	The {@link Temperature} parameter.
	 *
	 * @return				The difference between the {@link Monitor}'s temperature threshold
	 * 						and the given {@link Temperature}'s value.
	 */
	private Double computeTemperatureHeatingMargin(Map<String, String> metadata, NumberParam temperature) {

		notNull(metadata, "metadata cannot be null.");

		String warningThreshold = metadata.get(WARNING_THRESHOLD);

		String threshold = warningThreshold != null
			? warningThreshold
			: metadata.get(ALARM_THRESHOLD);

		if (threshold == null) {
			return null;
		}

		double thresholdValue;
		try {

			thresholdValue = Double.parseDouble(threshold);

		} catch (NumberFormatException e) {

			log.warn("Invalid threshold value: {}.", threshold);
			return null;
		}

		Double temperatureValue = temperature.getValue();

		return temperatureValue != null
			? thresholdValue - temperatureValue
			: null;
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
		targetMonitor.addParameter(targetHeatingMargin);
	}

	@Override
	public void release() {
		// Not implemented yet
	}

	@Override
	public void post() {

		// Refresh present parameters
		refreshPresentParameters();

		// Setting the target total energy
		aggregateTargetEnergy();

		// Setting the target heating margin
		computeTargetHeatingMargin();
	}
}
