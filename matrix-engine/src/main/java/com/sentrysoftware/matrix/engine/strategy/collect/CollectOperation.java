package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AMBIENT_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPILED_FILE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTED_PORTS_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTED_PORTS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HEATING_MARGIN_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_METER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SHARE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SOURCE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TOTAL_BANDWIDTH_PARAMETER;
import static org.springframework.util.Assert.state;

import java.math.RoundingMode;
import java.util.ArrayList;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.LinkStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
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
import com.sentrysoftware.matrix.model.alert.AlertInfo;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring.PowerMeter;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectOperation extends AbstractStrategy {

	private static final String NO_SOURCE_TABLE_CREATE_MSG = "Hostname {} - Collect - No source table created with source key {} for connector {}.";
	static final String NO_HW_MONITORS_FOUND_MSG = "Hostname {} - Collect - No hardware monitors found in connector {}. Collect operation will now be stopped.";

	@Override
	public void prepare() {
		// Save parameters, push current value to previous value
		// Why ? Before the next collect we save the parameters previous values
		// in order to compute delta and rates
		strategyConfig.getHostMonitoring().saveParameters();

		// Set the trigger and the alert information on each alert rule
		prepareAlertRules();

	}

	@Override
	public Boolean call() throws Exception {
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		log.debug("Hostname {} - Collect - Start collect", hostname);

		// Get the connectors previously discovered
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final Map<String, Monitor> connectorMonitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);

		final Map<String, Monitor> hostMonitors = hostMonitoring.selectFromType(MonitorType.HOST);

		if (hostMonitors != null && !hostMonitors.isEmpty()) {

			final Optional<Monitor> hostMonitor = hostMonitors.values().stream().findAny();

			if (hostMonitor.isPresent()) {

				final MonitorCollectVisitor visitor = new MonitorCollectVisitor(
					MonitorCollectInfo.builder()
						.engineConfiguration(strategyConfig.getEngineConfiguration())
						.collectTime(strategyTime)
						.hostMonitoring(hostMonitoring)
						.monitor(hostMonitor.get())
						.hostname(hostname)
						.matsyaClientsExecutor(matsyaClientsExecutor)
						.build());
				hostMonitor.get().getMonitorType().getMetaMonitor().accept(visitor);
			}
		}

		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error("Hostname {} - Collect - No connectors detected in the detection operation. Collect operation will now be stopped.",
					hostname);
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

		log.debug("Hostname {} - Collect - Processing connector {}.", hostname, connector.getCompiledFilename());

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

		final Stream<HardwareMonitor> hardwareMonitors = connector
				.getHardwareMonitors()
				.stream()
				.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
					&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
					&& !HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()));

		// The user may want to run queries in sequential mode
		if (strategyConfig.getEngineConfiguration().isSequential()) {

			log.info("Hostname {} - Running collect in sequential mode. Connector: {}.", hostname, connector.getCompiledFilename());

			hardwareMonitors.forEach(
					hardwareMonitor -> collectSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostname));

		} else {

			log.info("Hostname {} - Running collect in parallel mode. Connector: {}.", hostname, connector.getCompiledFilename());

			// Now collecting the rest of the monitors in parallel mode. (Default mode)
			final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

			hardwareMonitors
				.forEach(hardwareMonitor ->
					threadsPool.execute(
						() -> collectSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostname)
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

				log.debug("Hostname {} - Threads' termination aborted with an error: ", hostname, e);
			}
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

		log.debug("Hostname {} - Start Connector Monitor {} Collect.", hostname, connectorMonitor.getId());

		log.debug("Hostname {} - Start Connector {} Test.", hostname, connector.getCompiledFilename());

		final TestedConnector testedConnector = super.testConnector(connector, hostname);

		log.debug("Hostname {} - End of Test for Connector {}. Test Status: {}.", hostname, connector.getCompiledFilename(), getTestedConnectorStatus(testedConnector));

		final IParameter[] statusAndStatusInformation = super.buildConnectorStatusAndStatusInformation(testedConnector);
		final TextParam testReport = super.buildTestReportParameter(hostname, testedConnector);

		final DiscreteParam status = (DiscreteParam) statusAndStatusInformation[0];

		connectorMonitor.collectParameter(status); // Status
		connectorMonitor.collectParameter(statusAndStatusInformation[1]); // Status Information
		connectorMonitor.collectParameter(testReport);

		log.debug("Hostname {} - End of the Connector Monitor {} Collect. Status: {}.", hostname, connectorMonitor.getId(), status.getState());
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
			log.debug(NO_SOURCE_TABLE_CREATE_MSG, hostname, valueTable, connectorName);
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
				log.warn("Hostname {} - Collect - Could not find monitor {} associated with row {} for connector {}.",
						hostname, monitorType.getNameInConnector(), row, connectorName);
				continue;
			}

			log.debug("Hostname {} - Collecting monitor ID {}.", hostname, monitorOpt.get().getId());

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
				deviceIdValueTableColumn,
				strategyConfig.getEngineConfiguration().getHost().getHostname());

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

			log.debug("Hostname {} - Collecting monitor ID {}.", hostname, monitor.getId());

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

		final DiscreteParam presentParam = monitor.getParameter(PRESENT_PARAMETER, DiscreteParam.class);
		if (presentParam != null && Present.MISSING.equals(presentParam.getState())) {
			return;
		}

		// Get the source table used to collect parameters
		final SourceTable sourceTable = hostMonitoring
				.getConnectorNamespace(connectorName)
				.getSourceTable(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.debug(NO_SOURCE_TABLE_CREATE_MSG, hostname, valueTable, connectorName);
			return;
		}

		// Make sure the table is not empty
		if (sourceTable.getTable().isEmpty()) {
			log.error("Hostname {} - Collect - Empty source table created with source key {} for connector {}.",
					hostname, valueTable, connectorName);
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
			log.warn("Hostname {} - Collect - No monitor types were specified for hardware monitor job with connector {}.",
					hostname, connectorName);
			return false;
		}

		if (hardwareMonitor.getCollect() == null) {
			log.warn("Hostname {} - Collect - No {} monitor job specified during the collect for the connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
			return false;
		}

		// Check the collectType
		if (hardwareMonitor.getCollect().getType() == null) {
			log.warn("Hostname {} - Collect - No collect type found with {} during the collect for the connector {}. The default type (MONO_INSTANCE) will be used.",
					hostname, monitorType.getNameInConnector(), connectorName);
			hardwareMonitor.getCollect().setType(CollectType.MONO_INSTANCE);
		}

		// Check the collect parameters, so later in the code we can create the monitor with the metadata
		final Map<String, String> parameters = hardwareMonitor.getCollect().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("Hostname {} - Collect - No parameter found with {} during the collect for the connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
			return false;
		}

		// Check the valueTable key
		if (hardwareMonitor.getCollect().getValueTable() == null) {
			log.error("Hostname {} - Collect - No valueTable found with monitor {} for connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
			return false;
		}

		return true;
	}

	/**
	 * Refresh the collect time of the {@link Present} parameter in the given {@link Monitor} instance.
	 *
	 * @param monitor		The {@link Monitor} whose {@link Present}'s collect time should be refreshed.
	 * @param collectTime	The new collect time.
	 */
	static void refreshPresentCollectTime(final Monitor monitor, final Long collectTime) {
		final DiscreteParam presentParam = monitor.getParameter(PRESENT_PARAMETER, DiscreteParam.class);
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
	 * Setting the host power consumption value as the sum of all the {@link Enclosure}s' power consumption values.
	 */
	void sumEnclosurePowerConsumptions(@NonNull final Map<String, Monitor> enclosureMonitors) {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();

		// Getting the host monitor
		final Monitor hostMonitor = getHostMonitor(strategyConfig.getHostMonitoring());

		// Getting the sums of the enclosures' power consumption values
		Double totalPowerConsumption = enclosureMonitors
				.values()
				.stream()
				.filter(monitor -> !monitor.isMissing())
				.map(monitor -> CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER))
				.filter(Objects::nonNull)
				.reduce(Double::sum)
				.orElse(null);

		if (totalPowerConsumption == null) {
			// Let's try next collect
			log.debug("Hostname {} - The power consumption is going to be collected during the next collect.", hostname);
			return;
		}

		// Collect the power consumption and energy
		CollectHelper.collectEnergyUsageFromPower(
				hostMonitor,
				strategyTime,
				totalPowerConsumption,
				hostname
		);

		log.debug("Hostname {} - The power consumption has been collected. Value: {} Watts.", hostname, totalPowerConsumption);

	}

	/**
	 * Check if at least one monitor in the given map collects the power consumption or the energy
	 *
	 * @param monitors map of monitors
	 * @return boolean value
	 */
	static boolean isPowerCollected(final Map<String, Monitor> monitors) {
		return Optional
				.ofNullable(monitors)
				.stream()
				.map(Map::values)
				.flatMap(Collection::stream)
				.anyMatch(monitor -> CollectHelper.getNumberParamValue(monitor, ENERGY_PARAMETER) != null
						|| CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER) != null);
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

		return (temperatureValue != null && warningThresholdValue != null)
			? Math.max(warningThresholdValue - temperatureValue, 0.0)
			: null;
	}

	/**
	 * Setting the host heating margin value as the minimum value of all the {@link Temperature}s' heating margins.
	 */
	private void computeHostHeatingMargin() {

		IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		state(hostMonitoring != null, "hostMonitoring should not be null.");

		// Getting the host monitor
		Monitor hostMonitor = getHostMonitor(hostMonitoring);

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
		NumberParam hostHeatingMargin = NumberParam
			.builder()
			.name(HEATING_MARGIN_PARAMETER)
			.unit(HEATING_MARGIN_PARAMETER_UNIT)
			.collectTime(strategyTime)
			.value(minimumHeatingMargin)
			.rawValue(minimumHeatingMargin)
			.build();

		// Adding the parameter to the host monitor
		hostMonitor.collectParameter(hostHeatingMargin);
	}

	@Override
	public void release() {
		// Not implemented yet
	}

	/**
	 * Compute temperature parameters for the current host monitor:
	 * <ul>
	 * <li><b>ambientTemperature</b>: the minimum temperature between 5 and 100 degrees Celsius</li>
	 * <li><b>cpuTemperature</b>: the average CPU temperatures</li>
	 * <li><b>cpuThermalDissipationRate</b>: the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit)</li>
	 * </ul>
	 */
	void computeHostTemperatureParameters() {
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> temperatureMonitors = hostMonitoring
				.selectFromType(MonitorType.TEMPERATURE);

		final Monitor hostMonitor = getHostMonitor(hostMonitoring);

		// No temperatures then no computation
		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			log.debug(
					"Hostname {} - Could not compute temperature parameters (ambientTemperature, cpuTemperature, cpuThermalDissipationRate)",
					strategyConfig.getEngineConfiguration().getHost().getHostname());
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
				hostMonitor,
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
				hostMonitor,
				CPU_TEMPERATURE_PARAMETER,
				TEMPERATURE_PARAMETER_UNIT,
				strategyTime,
				cpuTemperatureAverage,
				cpuTemperatureAverage
			);

			// Calculate the dissipation rate
			computeHostThermalDissipationRate(hostMonitor, ambientTemperature, cpuTemperatureAverage);
		}

	}

	/**
	 * Calculate the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit).
	 *
	 * @param hostMonitor           The host monitor we wish to update its heat dissipation rate
	 * @param ambientTemperature    The ambient temperature of the host
	 * @param cpuTemperatureAverage The CPU average temperature previously computed based on the cpu sensor count
	 */
	void computeHostThermalDissipationRate(final Monitor hostMonitor, final double ambientTemperature, final double cpuTemperatureAverage) {

		// Get the average CPU temperature computed at the discovery level
		final double ambientToWarningDifference = NumberHelper.parseDouble(
				hostMonitor.getMetadata(AVERAGE_CPU_TEMPERATURE_WARNING), 0.0) - ambientTemperature;

		// Avoid the arithmetic exception
		if (ambientToWarningDifference != 0.0) {
			double cpuThermalDissipationRate = (cpuTemperatureAverage - ambientTemperature) / ambientToWarningDifference;

			// Do we have a consistent fraction
			if (cpuThermalDissipationRate >= 0 && cpuThermalDissipationRate <= 1) {

				cpuThermalDissipationRate = NumberHelper.round(cpuThermalDissipationRate, 2, RoundingMode.HALF_UP);

				CollectHelper.updateNumberParameter(
					hostMonitor,
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
	 * Compute network card parameters for the current host monitor:
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
			final IState linkStatus = CollectHelper.getParameterState(networkCardMonitor, LINK_STATUS_PARAMETER);

			// If there is connected count it.
			if (LinkStatus.PLUGGED.equals(linkStatus)) {
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

		// Add the new parameters to the host monitor
		final Monitor hostMonitor = getHostMonitor(hostMonitoring);
		hostMonitor.collectParameter(connectedPortsCountParam);
		hostMonitor.collectParameter(totalBandwidthParam);
	}

	@Override
	public void post() {

		// Refresh present parameters
		refreshPresentParameters();

		// Setting the host heating margin
		computeHostHeatingMargin();

		// Compute temperatures
		computeHostTemperatureParameters();

		// Estimate power consumption for DiskControllers, Memories and PhysicalDisks.
		// This estimation is computed here, as a post collect, because it doesn't rely on the collected parameters
		// that are currently computed through the MonitorCollectVisitor.
		// Also, if the connector doesn't define the collect job for the monitorType (e.g. CpqIDEDriveArray.hdf)
		// we don't want to skip the estimation.
		estimateDiskControllersPowerConsumption();
		estimateMemoriesPowerConsumption();
		estimatePhysicalDisksPowerConsumption();

		// Estimate CPUs Power Consumption
		// The CPUs power consumption needs to be estimated in the post collect strategy
		// because the computation requires the host Thermal Dissipation Rate which is also collected at the end of the collect.
		estimateCpusPowerConsumption();

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		// Compute the power consumption
		computeHostPowerConsumption(hostMonitoring);

		// Estimate the VMs Power Consumption
		// The VMs power consumption needs to be estimated in the post collect strategy
		// because it requires the power consumption of the device whose power source the VMs are consuming
		estimateVmsPowerConsumption();

		// Set the power meter metadata
		final PowerMeter powerMeter = hostMonitoring.getPowerMeter();
		getHostMonitor(hostMonitoring)
			.addMetadata(POWER_METER, powerMeter != null ? powerMeter.name().toLowerCase() : null);
	}

	/**
	 * Compute the host's power consumption
	 *
	 * @param hostMonitoring
	 */
	void computeHostPowerConsumption(final IHostMonitoring hostMonitoring) {

		// Getting the enclosure monitors
		final Map<String, Monitor> enclosureMonitors = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		if (isPowerCollected(enclosureMonitors)) {
			// Set power meter to collected
			hostMonitoring.setPowerMeter(PowerMeter.MEASURED);

			sumEnclosurePowerConsumptions(enclosureMonitors);
		} else {
			// Set power meter to estimated
			hostMonitoring.setPowerMeter(PowerMeter.ESTIMATED);

			// Estimate the host Power Consumption
			// The host estimated power consumption is the sum of all monitor's power consumption that are not missing (Present = 1) divided by 0.9, to
			// account for the power supplies' heat dissipation (90% efficiency assumed).
			estimateHostPowerConsumption();
		}
	}

	/**
	 * Estimate the host power consumption.<br> Perform the the sum of all monitor's power consumption, energy and energy usage, excluding missing
	 * monitors. The final value is divided by 0.9 to add 10% to the final value so that we account the power supplies' heat dissipation (90%
	 * efficiency assumed)
	 */
	void estimateHostPowerConsumption() {

		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();

		// Browse through all the collected objects and perform the sum of parameters using the map-reduce
		final Double totalPowerConsumption = hostMonitoring.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> !monitor.isMissing()) // Skip missing
			.filter(monitor -> !MonitorType.HOST.equals(monitor.getMonitorType())) // We already sum the values for the host
			.filter(monitor -> !MonitorType.ENCLOSURE.equals(monitor.getMonitorType())) // Skip the enclosure
			.filter(monitor -> !MonitorType.VM.equals(monitor.getMonitorType())) // Skip VM monitors as their power is already computed based on the host's power
			.map(monitor -> CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER))
			.filter(Objects::nonNull) // skip null power consumption values
			.reduce(Double::sum)
			.orElse(null);

		if (totalPowerConsumption == null) {
			log.debug("Hostname {} - No power consumption estimated for the monitored devices.", hostname);
			return;
		}

		// Getting the host monitor
		final Monitor hostMonitor = getHostMonitor(hostMonitoring);

		// Add 10% because of the heat dissipation of the power supplies
		final double powerConsumption = NumberHelper.round(totalPowerConsumption / 0.9, 2, RoundingMode.HALF_UP);
		if (powerConsumption > 0) {
			CollectHelper.collectEnergyUsageFromPower(
					hostMonitor,
					strategyTime,
					powerConsumption,
					hostname);
			log.debug("Hostname {} - Power Consumption: Estimated at {} Watts.", hostname, powerConsumption);

		} else {
			log.warn("Hostname {} - Power Consumption could not be estimated. Negative value: {}.", hostname, powerConsumption);
		}

	}

	/**
	 * Estimates the power consumption of the processors
	 */
	void estimateCpusPowerConsumption() {
		final String hostname = strategyConfig
				.getEngineConfiguration()
				.getHost().getHostname();
		final Long collectTime = this.strategyTime;
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> cpus = hostMonitoring.selectFromType(MonitorType.CPU);

		if (cpus == null) {
			log.debug("Hostname {} - No CPU discovered. Skipping CPUs' Power Consumption estimation.", hostname);
			return;
		}

		final Monitor host = getHostMonitor(hostMonitoring);

		cpus.values()
			.stream()
			.filter(cpu -> !cpu.isMissing())
			.forEach(cpu -> estimateCpuPowerConsumption(cpu, host, collectTime, hostname));
	}

	/**
	 * Estimates the power dissipation of a processor, based on some characteristics Inspiration:
	 * https://en.wikipedia.org/wiki/List_of_CPU_power_dissipation_figures Page 11 of
	 * http://www.cse.iitd.ernet.in/~srsarangi/files/papers/powersurvey.pdf
	 *
	 * @param cpu         The CPU monitor we wish to estimate its power dissipation
	 * @param host        The host root parent monitor from which we extract the overall dissipation rate of the processors
	 * @param collectTime The current strategy collect time
	 * @param hostname    The system hostname
	 */
	void estimateCpuPowerConsumption(@NonNull final Monitor cpu, @NonNull final Monitor host,
			@NonNull final Long collectTime, @NonNull String hostname) {

		Double maximumPowerConsumption = NumberHelper.parseDouble(cpu.getMetadata(POWER_CONSUMPTION), null);

		// If we didn't get the actual maximum power consumption, discovered by the DiscoveryOperation strategy, assume it's 19W/GHz
		if (maximumPowerConsumption == null || maximumPowerConsumption < 0) {

			// Get the maximum speed, discovered metadata.
			double maximumSpeed = NumberHelper.parseDouble(cpu.getMetadata(MAXIMUM_SPEED), 0.0);

			// No processor speed? Assume 2.5GHz for the calculation
			if (maximumSpeed <= 0) {
				maximumSpeed = 2500.0;
			}

			maximumPowerConsumption = maximumSpeed / 1000 * 19.0;
		}

		// Get the thermal dissipation rate collected on the host monitor at the end of the collect
		Double thermalDissipationRate = CollectHelper.getNumberParamValue(host, CPU_THERMAL_DISSIPATION_RATE_PARAMETER);

		// If we didn't have a thermal dissipation rate value, then assume it's at 25%
		if (thermalDissipationRate == null) {
			thermalDissipationRate = 0.25;
		}

		// Compute the estimated power consumption
		final double powerConsumption = NumberHelper.round(maximumPowerConsumption * thermalDissipationRate, 2, RoundingMode.HALF_UP);

		// This will set the energy, the delta energy called energyUsage and the powerConsumption on the cpu monitor
		CollectHelper.collectEnergyUsageFromPower(cpu, collectTime, powerConsumption, hostname);
	}

	/**
	 * Estimates the power consumption, energy and energy usage values of all online VMs.
	 */
	void estimateVmsPowerConsumption() {

		IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		// Getting all the VMs
		Map<String, Monitor> allVMsById = hostMonitoring.selectFromType(MonitorType.VM);
		if (allVMsById == null || allVMsById.isEmpty()) {
			return;
		}

		Collection<Monitor> allVms = allVMsById.values();

		// Getting all the power shares by power source ID (only for online VMs)
		Map<String, Double> totalPowerSharesByPowerSource = allVms
				.stream()
				.collect(Collectors.toMap(vm ->
							getVmPowerSourceMonitorId(vm, hostMonitoring),
							this::getVmPowerShare,
							Double::sum
						)
				);

		// Setting the power consumption and energyUsage for each online VM
		allVms
			.stream()
			.forEach(vm -> estimateVmPowerConsumption(vm, totalPowerSharesByPowerSource, hostMonitoring));
	}

	/**
	 * Get the VM's power share which is assumed not null and >= 0.0
	 *
	 * @param vm VM {@link Monitor} instance
	 * @return Double value. Returns 0.0 if the power share is null or less than 0.0 or the VM is not online
	 */
	double getVmPowerShare(Monitor vm) {

		if (!isVmOnline(vm)) {
			return 0.0;
		}

		final Double powerShare = CollectHelper.getNumberParamValue(vm, POWER_SHARE_PARAMETER);
		if (powerShare != null && powerShare >= 0.0) {
			return powerShare;
		}

		return 0.0;
	}

	/**
	 * Estimates the power consumption, energy and energy usage values of the given VM.
	 *
	 * @param vm							The VM whose consumption values should be estimated.
	 * @param totalPowerSharesByPowerSource	A {@link Map} associating each power source {@link Monitor} ID
	 *                                      to the sum of all power shares of the VMs consuming power from it.
	 * @param hostMonitoring				The {@link IHostMonitoring} instance wrapping all {@link Monitor}s.
	 */
	void estimateVmPowerConsumption(Monitor vm, Map<String, Double> totalPowerSharesByPowerSource,
									IHostMonitoring hostMonitoring) {

		// Get the vm power share, always >= 0.0 here
		final double vmPowerShare = getVmPowerShare(vm);

		// Getting the VM's power share ratio
		String powerSourceId = vm.getMetadata(POWER_SOURCE_ID);
		Double totalPowerShares = totalPowerSharesByPowerSource.get(powerSourceId);

		// totalPowerShares is never null here because the VM always comes with a powerShare value
		double powerShareRatio = totalPowerShares != 0.0 ? vmPowerShare / totalPowerShares : 0.0;

		// Getting the power source's power consumption value
		Monitor powerSourceMonitor = hostMonitoring.findById(powerSourceId);

		Double powerSourcePowerConsumption = CollectHelper.getNumberParamValue(powerSourceMonitor,
				POWER_CONSUMPTION_PARAMETER);

		// Setting the VM's power consumption, energy and energy usage values
		if (powerSourcePowerConsumption != null && powerSourcePowerConsumption >= 0.0) {

			double powerConsumption = NumberHelper.round(powerSourcePowerConsumption * powerShareRatio, 2,
				RoundingMode.HALF_UP);

			// This will set the energy, the delta energy called energyUsage and the powerConsumption on the VM monitor
			CollectHelper.collectEnergyUsageFromPower(
				vm,
				strategyTime,
				powerConsumption,
				strategyConfig
					.getEngineConfiguration()
					.getHost()
					.getHostname()
			);
		}
	}

	/**
	 * @param vm	The VM whose online status should be determined.
	 *
	 * @return		Whether or not the given VM is online.
	 */
	boolean isVmOnline(Monitor vm) {

		return PowerState.ON.equals(CollectHelper.getParameterState(vm, POWER_STATE_PARAMETER));
	}

	/**
	 * @return The ID of the parent {@link Monitor} whose power source is consumed by the given VM.
	 */
	String getVmPowerSourceMonitorId(Monitor vm, IHostMonitoring hostMonitoring) {

		// If the parent has a power consumption, then we have the power source
		Monitor parent = hostMonitoring.findById(vm.getParentId());
		if (parent != null && parent.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class) != null) {

			vm.addMetadata(POWER_SOURCE_ID, parent.getId());
			return parent.getId();
		}

		// If the parent does not have a power consumption, the power source is the host
		Monitor hostMonitor = getHostMonitor(hostMonitoring);
		vm.addMetadata(POWER_SOURCE_ID, hostMonitor.getId());

		return hostMonitor.getId();
	}

	/**
	 * Set the power consumption (15W by default for disk controllers) Source:
	 * https://forums.servethehome.com/index.php?threads/raid-controllers-power-consumption.9189/
	 */
	void estimateDiskControllersPowerConsumption() {
		final String hostname = strategyConfig
				.getEngineConfiguration()
				.getHost().getHostname();
		final Long collectTime = this.strategyTime;
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> diskControllers = hostMonitoring.selectFromType(MonitorType.DISK_CONTROLLER);

		if (diskControllers == null) {
			log.debug("Hostname {} - No Disk Controllers discovered. Skipping Disk Controllers' Power Consumption estimation.", hostname);
			return;
		}

		diskControllers.values()
			.stream()
			.filter(dc -> !dc.isMissing())
			.forEach(dc -> CollectHelper.collectEnergyUsageFromPower(
				dc,
				collectTime,
				15.0,
				hostname
			));
	}

	/**
	 * Estimated power consumption: 4W
	 * Source: https://www.buildcomputers.net/power-consumption-of-pc-components.html
	 */
	void estimateMemoriesPowerConsumption() {
		final String hostname = strategyConfig
				.getEngineConfiguration()
				.getHost().getHostname();
		final Long collectTime = this.strategyTime;
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> memories = hostMonitoring.selectFromType(MonitorType.MEMORY);

		if (memories == null) {
			log.debug("Hostname {} - No Memories discovered. Skipping Memories' Power Consumption estimation.", hostname);
			return;
		}

		memories.values()
			.stream()
			.filter(memory -> !memory.isMissing())
			.forEach(memory -> CollectHelper.collectEnergyUsageFromPower(
				memory,
				collectTime,
				4.0,
				hostname
			));
	}

	/**
	 * Estimates the power dissipation for each physical disk, based on its characteristics:
	 * vendor, model, location, type, etc. all mixed up
	 * Inspiration: https://outervision.com/power-supply-calculator
	 */
	void estimatePhysicalDisksPowerConsumption() {
		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		final Long collectTime = this.strategyTime;
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();
		final Map<String, Monitor> physicalDisks = hostMonitoring.selectFromType(MonitorType.PHYSICAL_DISK);

		if (physicalDisks == null) {
			log.debug("Hostname {} - No Physical Disks discovered. Skipping Physical Disks' Power Consumption estimation.",
					hostname);
			return;
		}

		physicalDisks
			.values()
			.stream()
			.filter(disk -> !disk.isMissing())
			.forEach(monitor -> {

				// Physical disk characteristics
				final List<String> dataList = new ArrayList<>();
				dataList.add(monitor.getName());
				dataList.add(monitor.getMetadata(MODEL));
				dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION1));
				dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION2));
				dataList.add(monitor.getMetadata(ADDITIONAL_INFORMATION3));

				final Monitor parent = hostMonitoring.findById(monitor.getParentId());
				if (parent != null) {
					dataList.add(parent.getName());
				} else {
					log.error("Hostname {} - No parent found for the Physical Disk identified by: {}. Physical disk name: {}.",
							hostname, monitor.getId(), monitor.getName());
				}

				final String[] data = dataList.toArray(new String[0]);

				final double powerConsumption;

				// SSD
				if (ArrayHelper.anyMatchLowerCase(str -> str.contains("ssd") || str.contains("solid"), data)) {
					powerConsumption = estimateSsdPowerConsumption(data);
				}

				// HDD (non-SSD), depending on the interface
				// SAS
				else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("sas"), data)) {
					powerConsumption = estimateSasPowerConsumption(data);
				}

				// SCSI and IDE
				else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("scsi") || str.contains("ide"), data)) {
					powerConsumption = estimateScsiAndIde(data);
				}

				// SATA (and unknown, we'll assume it's the most common case)
				else {
					powerConsumption = estimateSataOrDefault(data);
				}

				CollectHelper.collectEnergyUsageFromPower(monitor, collectTime, powerConsumption, hostname);

			});
	}

	/**
	 * Estimate SATA physical disk power dissipation. Default is 11W.
	 *
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSataOrDefault(final String[] data) {

		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			return 27.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			return 7.0;
		}

		// Default for 7200-RPM disks
		return 11.0;

	}

	/**
	 * Estimate SCSI and IDE physical disk power dissipation
	 *
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateScsiAndIde(final String[] data) {
		// SCSI and IDE
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			// Only SCSI supports 10k
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			// Only SCSI supports 15k
			return 35.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			// Likely to be cheap IDE
			return 19;
		}

		// Default for 7200-rpm disks, SCSI or IDE, who knows?
		// SCSI is 31 watts, IDE is 21...
		return 30.0;
	}

	/**
	 * Estimate SAS physical disk power dissipation
	 *
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSasPowerConsumption(final String[] data) {
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 17.0;
		}
		// Default for 10k-rpm disks (rarely lower than that anyway)
		return 12.0;
	}

	/**
	 * Estimate SSD physical disk power dissipation
	 *
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSsdPowerConsumption(final String[] data) {
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("pcie"), data)) {
			return 18.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("nvm"), data)) {
			return  6.0;
		}
		return 3.0;
	}

	/**
	 * Prepare all the alert rules before starting the collect.<br>
	 * This method sets the alert trigger {@link Consumer} and the alert information
	 * {@link AlertInfo} used to build the final alert content on each alert rule.
	 */
	void prepareAlertRules() {
		// Inject the alert trigger and alert information in each alert rule
		// We set the values in the collect because an engine wrapper (e.g. hws-agent)
		// could build the alert trigger (consumer) only after the discovery is completed as it requires discovered information
		final Consumer<AlertInfo> alertTrigger = strategyConfig.getEngineConfiguration().getAlertTrigger();
		if (alertTrigger != null) {
			// Flatten all the monitors
			final Stream<Monitor> allMonitors = strategyConfig.getHostMonitoring()
					.getMonitors()
					.values()
					.stream()
					.map(Map::values)
					.flatMap(Collection::stream);

			// Loop over each monitor then each alert rule and set the required attributes
			allMonitors.forEach(monitor ->

				// Loop over all the alert rules
				monitor.getAlertRules().entrySet().forEach(alertRulesEntry -> {
					final String parameterName = alertRulesEntry.getKey();
					// A parameter can have several alert rules
					alertRulesEntry.getValue().forEach(alertRule -> {
						// The trigger passed by the hws-agent
						alertRule.setTrigger(alertTrigger);
						// Create and set a new AlertInfo
						alertRule.setAlertInfo(AlertInfo
								.builder()
								.alertRule(alertRule)
								.monitor(monitor)
								.parameterName(parameterName)
								.hardwareHost(strategyConfig.getEngineConfiguration().getHost())
								.hostMonitoring(strategyConfig.getHostMonitoring())
								.build()
						);
					});
				})
			);
		}
	}
}
