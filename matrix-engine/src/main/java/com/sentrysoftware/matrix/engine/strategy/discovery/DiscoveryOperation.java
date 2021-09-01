package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.InstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.TextInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.PresentParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscoveryOperation extends AbstractStrategy {

	private static final String NO_HW_MONITORS_FOUND_MSG = "Could not discover system {}. No hardware monitors found in the connector {}";
	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile("^\\s*instancetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void prepare() {

		strategyConfig
			.getHostMonitoring()
			.backup();

		strategyConfig
			.getHostMonitoring()
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.forEach(monitor ->
				resetPresentParam(monitor.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class)));
	}

	@Override
	public Boolean call() throws Exception {

		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();
		log.debug("Start discovery for system {}", hostname);

		// Get the connectors previously created/selected in the DetectionOperation
		// strategy
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		// Get the Target monitor
		final Map<String, Monitor> targets = hostMonitoring.selectFromType(MonitorType.TARGET);

		if (targets == null) {
			log.error("No target found for system {}. Stop discovery operation", hostname);
			return false;
		}

		final Monitor targetMonitor = targets.values().stream().findFirst().orElse(null);
		if (targetMonitor == null) {
			log.error("No target monitor found for system {}. Stop discovery operation", hostname);
			return false;
		}

		final Map<String, Monitor> connectorMonitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);

		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error("No connector detected in the detection operation. Stop discovery operation");
			return false;
		}

		final Set<String> detectedConnectorNames = connectorMonitors.values().stream().map(Monitor::getName)
				.collect(Collectors.toSet());

		// Create a list with connectors defining enclosures on the top since we need to
		// discover the enclosures first
		final List<Connector> connectors = store
				.getConnectors()
				.entrySet()
				.stream()
				.filter(entry -> detectedConnectorNames.contains(entry.getKey()))
				.map(Entry::getValue)
				.sorted(new EnclosureFirstComparator())
				.collect(Collectors.toList());

		connectors.stream()
		.filter(connector -> super.validateHardwareMonitors(connector, hostname, NO_HW_MONITORS_FOUND_MSG))
		.forEach(connector -> discover(connector, hostMonitoring, hostname, targetMonitor));

		return true;
	}

	/**
	 * Run the discovery for the given connector
	 * 
	 * @param connector      The connector we wish to interpret and discover
	 * @param hostMonitoring The monitors container, it also wraps the {@link SourceTable} objects
	 * @param hostname       The system hostname
	 * @param targetMonitor  The main {@link MonitorType#TARGET} monitor detected in the {@link DetectionOperation} strategy.
	 */
	void discover(final Connector connector, final IHostMonitoring hostMonitoring, final String hostname,
			final Monitor targetMonitor) {

		log.debug("Processing connector {} for system {}", connector.getCompiledFilename(), hostname);

		// Perform discovery for the hardware monitor jobs
		// The discovery order is the following: Enclosure, Blade, DiskController, CPU then the rest
		// The order is important so that each monitor can be attached to its parent correctly
		// Start the discovery of the first order in serial mode
		connector
			.getHardwareMonitors()
			.stream()
			.sorted(new HardwareMonitorComparator())
			.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
				&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
				&& HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
			.forEach(hardwareMonitor -> discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring,
				targetMonitor, hostname));


		// Now discover the rest of the monitors in parallel mode
		// This might depend on a user configuration
		final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

		connector
		.getHardwareMonitors()
		.stream()
		.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
				&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
				&& !HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
		.forEach(hardwareMonitor ->
			threadsPool.execute(() -> discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, targetMonitor,
				hostname)));

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
	 * Discover monitors of the same type. This method processes all the sources of
	 * the discovery stage then creates the required monitors
	 * 
	 * @param hardwareMonitor Defines the discovery {@link InstanceTable}, the {@link Source}
	 *                        to process and all the metadata
	 * @param connector   	  The connector we currently process
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping
	 *                        {@link Monitor} and {@link SourceTable} instances
	 * @param targetMonitor   The target monitor (main)
	 * @param hostname        The user's configured hostname
	 */
	void discoverSameTypeMonitors(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final Monitor targetMonitor, final String hostname) {

		// Process all the sources with theirs computes
		processSourcesAndComputes(
				hardwareMonitor.getDiscovery().getSources(),
				hostMonitoring,
				connector,
				hardwareMonitor.getType(),
				hostname);

		// Create the monitors
		createSameTypeMonitors(
				connector.getCompiledFilename(),
				hostMonitoring,
				hardwareMonitor.getDiscovery().getInstanceTable(),
				hardwareMonitor.getDiscovery().getParameters(),
				targetMonitor,
				hardwareMonitor.getType(),
				hostname);

	}

	/**
	 * Create monitors from the same type, most of time this method is called with a {@link SourceInstanceTable},
	 * In that case we loop over all the rows referenced in the {@link InstanceTable}, for each row we create a new
	 * {@link Monitor} instance then we set the discovery metadata on each created monitor
	 * 
	 * @param connectorName  The unique name of the {@link Connector}. The compiled file name
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping source tables and monitors
	 * @param instanceTable  Defines the source key or the hard coded key
	 * @param parameters     The discovery parameters to process (from the connector)
	 * @param targetMonitor  The main monitor with {@link MonitorType#TARGET} type
	 * @param monitorType    The current type of the monitor, {@link MonitorType}
	 * @param hostname       The user's configured hostname used for debug purpose
	 */
	void createSameTypeMonitors(final String connectorName, final IHostMonitoring hostMonitoring,
			final InstanceTable instanceTable, final Map<String, String> parameters, final Monitor targetMonitor,
			final MonitorType monitorType, final String hostname) {

		final TargetType targetType = strategyConfig.getEngineConfiguration().getTarget().getType();

		// Process the instance table
		if (instanceTable instanceof SourceInstanceTable) {
			final SourceInstanceTable sourceInstanceTable = (SourceInstanceTable) instanceTable;
			final String sourceKey = sourceInstanceTable.getSourceKey();

			// No sourceKey no monitor
			if (sourceKey == null) {
				log.error(
						"No source key found with monitor {} for connector {} on system {}",
						monitorType.getName(), connectorName, hostname);
				return;
			}

			final SourceTable sourceTable = hostMonitoring.getSourceTableByKey(sourceKey);

			// No sourceTable no monitor
			if (sourceTable == null) {
				log.error(
						"No source table created with source key {} for connector {} on system {}",
						sourceKey, connectorName, hostname);
				return;
			}

			int idCount = 0;
			// Loop over each row (List) and create one monitor per row
			for (final List<String> row : sourceTable.getTable()) {

				final Monitor monitor = Monitor.builder().build();

				processSourceTableParameters(connectorName, parameters, sourceKey, row, monitor, idCount);

				final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
						.builder()
						.monitor(monitor)
						.targetMonitor(targetMonitor)
						.connectorName(connectorName)
						.hostMonitoring(hostMonitoring)
						.monitorType(monitorType)
						.hostname(hostname)
						.targetType(targetType)
						.build();

				monitorType.getMetaMonitor().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));

				idCount++;
			}

		} else {
			final Monitor monitor = Monitor.builder().build();

			processTextParameters(parameters, monitor, connectorName);

			final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
					.builder()
					.monitor(monitor)
					.targetMonitor(targetMonitor)
					.connectorName(connectorName)
					.hostMonitoring(hostMonitoring)
					.monitorType(monitorType)
					.hostname(hostname)
					.targetType(targetType)
					.build();

			monitorType.getMetaMonitor().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));
		}
	}

	/**
	 * Process the parameters defined in a {@link TextInstanceTable}. I.e. Hard
	 * coded instance table
	 * 
	 * @param parameters    Key-value map from the connector discovery instance used to create hard coded metadata
	 * @param monitor       The monitor on which we want to set the parameter values as metadata
	 * @param connectorName The name of the connector to be set as metadata
	 */
	void processTextParameters(final Map<String, String> parameters, final Monitor monitor, final String connectorName) {
		for (final Entry<String, String> parameter : parameters.entrySet()) {
			monitor.addMetadata(parameter.getKey(), parameter.getValue());
		}

		// Add the id count parameter
		monitor.addMetadata(HardwareConstants.ID_COUNT, String.valueOf(0));

		// Add the connector name to the metadata
		monitor.addMetadata(HardwareConstants.CONNECTOR, connectorName);
	}

	/**
	 * Process the given row to create discovery metadata on the {@link Monitor} instance
	 * 
	 * @param connectorName The connector unique name used for logging purpose
	 * @param parameters    Key-value map from the connector discovery instance used to create metadata
	 * @param sourceKey     The unique identifier of the source used for logging purpose
	 * @param row           The row from the {@link SourceTable} so that we can extract the value when
	 * 						we encounter the pattern `InstanceTable.Column($number)`
	 * @param monitor       The monitor on which we are going to set the metadata
	 * @param idCount		The id used to identify the monitor by its position in {@link SourceTable} lines
	 */
	void processSourceTableParameters(final String connectorName, final Map<String, String> parameters,
			final String sourceKey, final List<String> row, final Monitor monitor, final int idCount) {

		// Loop over all the key values defined in the connector's Instance and create a metadata attribute for each entry
		for (final Entry<String, String> parameter : parameters.entrySet()) {

			final String key = parameter.getKey();
			final String value = parameter.getValue();
			final Matcher matcher = INSTANCE_TABLE_PATTERN.matcher(value);

			// Means we have a column number so we can extract the value from the row
			if (matcher.find()) {
				final int columnIndex = Integer.parseInt(matcher.group(1)) - 1;

				if (columnIndex >= 0 && columnIndex < row.size()) {
					// Get the real value from the source table row
					monitor.addMetadata(key, row.get(columnIndex));

				} else {
					log.warn("Column {} doesn't match the instance table source {} for connector {}", columnIndex,
							sourceKey, connectorName);
				}
			} else {
				// Hard coded value
				monitor.addMetadata(key, value);
			}

		}

		// Add the idCount metadata
		monitor.addMetadata(HardwareConstants.ID_COUNT, String.valueOf(idCount));

		// Add the connector name to the metadata
		monitor.addMetadata(HardwareConstants.CONNECTOR, connectorName);

	}


	/**
	 * Return <code>true</code> if the following conditions are met
	 * <ol>
	 *     <li>The MonitorType of the given hardwareMonitor is not null</li>
	 *     <li>The Discovery of the given hardwareMonitor is not null</li>
	 *     <li>The InstanceTable of the given hardwareMonitor is not null</li>
	 *     <li>The parameters map of the given hardwareMonitor is not null or empty</li>
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

		// Is there any discovery job here ?
		final MonitorType monitorType = hardwareMonitor.getType();
		if (monitorType == null) {
			log.warn("No type specified for hardware monitor job with connector {} on system {}", connectorName, hostname);
			return false;
		}

		if (hardwareMonitor.getDiscovery() == null) {
			log.warn("No {} monitor job specified during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		// Check the instanceTable, so that, we can create the monitor later
		if (hardwareMonitor.getDiscovery().getInstanceTable() == null) {
			log.warn("No instance table found with {} during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		// Get the discovery parameters, so we can create the monitor with the metadata
		final Map<String, String> parameters = hardwareMonitor.getDiscovery().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("No parameter found with {} during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return false;
		}

		return true;
	}

	@Override
	public void post() {

		// Missing monitors
		handleMissingMonitorDetection(strategyConfig.getHostMonitoring());

		// Handle CPU temperatures
		handleCpuTemperatures(strategyConfig.getHostMonitoring());
	}

	/**
	 * Detect the CPU temperature sensors and set the average CPU temperature warning 
	 * 
	 * @param hostMonitoring The wrapper of the monitors
	 */
	void handleCpuTemperatures(final IHostMonitoring hostMonitoring) {
		final Map<String, Monitor> temperatureMonitors = hostMonitoring
				.selectFromType(MonitorType.TEMPERATURE);

		final Monitor targetMonitor = getTargetMonitor(hostMonitoring);

		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			log.debug(
					"Could not detect cpu temperature sensors on the given host: {}. isCpuSensor and averageCpuTemperatureWarning metadata won't be set.",
					strategyConfig.getEngineConfiguration().getTarget().getHostname());
			return;
		}

		double cpuTemperatureSensorCount = 0;
		double cpuTemperatureWarningAverage = 0.0;

		// Loop over all the temperature monitors to detect cpu temperature sensors then compute the CPU temperature warning 
		for (final Monitor temperatureMonitor : temperatureMonitors.values()) {

			final String name = temperatureMonitor.getName();
			final String additionalInformation1 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION1);
			final String additionalInformation2 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION2);
			final String additionalInformation3 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION3);
			final Double warningThreshold = getTemperatureWarningThreshold(temperatureMonitor.getMetadata());

			// Is this a cpu sensor? check all the information name, addtional information and the warning threshold 
			if (isCpuSensor(warningThreshold, name, additionalInformation1, additionalInformation2, additionalInformation3)) {
				temperatureMonitor.addMetadata(IS_CPU_SENSOR, Boolean.TRUE.toString());
				cpuTemperatureSensorCount++;
				cpuTemperatureWarningAverage += warningThreshold;
			}
		}

		if (cpuTemperatureSensorCount != 0) {

			// Calculate the average value
			cpuTemperatureWarningAverage /= cpuTemperatureSensorCount;

			// Set the cpuTemperatureWarningAverage value as String
			targetMonitor.addMetadata(AVERAGE_CPU_TEMPERATURE_WARNING, String.valueOf(cpuTemperatureWarningAverage));
		}
	}

	/**
	 * Check if the given information match a CPU sensor
	 * 
	 * @param warningThreshold The warning threshold previously computed
	 * @param data             The string data to check
	 * @return <code>true</code> the warning threshold is greater than 10 degrees and the data contains "cpu" or "proc" otherwise
	 *         <code>false</code>
	 */
	static boolean isCpuSensor(final Double warningThreshold, final String... data) {
		return warningThreshold != null && warningThreshold > 10 && data != null
				&& Arrays.stream(data)
				.filter(Objects::nonNull)
				.map(String::toLowerCase)
				.anyMatch(DiscoveryOperation::matchesCpuSensor);
	}

	/**
	 * Check whether the given value matches a CPU sensor
	 * 
	 * @param value string value to check
	 * @return <code>true</code> if the given value matches "cpu" or "proc" otherwise <code>false</code>
	 */
	static boolean matchesCpuSensor(final String value) {
		return value.contains("cpu") || value.contains("proc");
	}

	/**
	 * Detect Missing Monitors 
	 * 
	 * @param hostMonitoring The wrapper of the monitors
	 */
	void handleMissingMonitorDetection(final IHostMonitoring hostMonitoring) {

		final Set<Monitor> previousMonitors = hostMonitoring
			.getPreviousMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());

		final Set<Monitor> currentMonitors = hostMonitoring
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.collect(Collectors.toSet());

		final Set<String> currentMonitorIds = currentMonitors
			.stream()
			.map(Monitor::getId)
			.collect(Collectors.toSet());

		previousMonitors.forEach(monitor -> processMissing(monitor, currentMonitorIds, hostMonitoring));

		currentMonitors
			.stream()
			.filter(monitor -> monitor.getMonitorType().getMetaMonitor().hasPresentParameter())
			.filter(monitor -> monitor.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class) != null)
			.filter(monitor ->
				monitor.getParameter(HardwareConstants.PRESENT_PARAMETER, PresentParam.class).getPresent() == null)
			.forEach(Monitor::setAsMissing);
	}

	/**
	 * Process missing monitor
	 * 
	 * @param previousMonitor   The previous monitor instance we wish to check
	 * @param currentMonitorIds The current identifiers of the discovered monitor instances
	 * @param hostMonitoring    The monitors wrapper
	 */
	static void processMissing(final Monitor previousMonitor, final Set<String> currentMonitorIds,
			final IHostMonitoring hostMonitoring) {

		if (currentMonitorIds.contains(previousMonitor.getId())) {
			return;
		}

		hostMonitoring.addMissingMonitor(previousMonitor);
	}

	/**
	 * Reset the present parameter
	 * 
	 * @param presentParam The parameter we wish to reset
	 */
	static void resetPresentParam(final PresentParam presentParam) {

		if (presentParam != null) {
			presentParam.discoveryReset();
		}
	}

	@Override
	public void release() {
		// Not implemented yet
	}

}
