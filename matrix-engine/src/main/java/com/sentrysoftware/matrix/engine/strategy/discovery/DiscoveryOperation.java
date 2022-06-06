package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPILED_FILE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IS_CPU_SENSOR;

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
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
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
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import com.sentrysoftware.matrix.engine.host.HostType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscoveryOperation extends AbstractStrategy {

	private static final String NO_HW_MONITORS_FOUND_MSG = "Hostname {} - Discovery failed. No hardware monitors found in the connector {}.";
	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile("^\\s*instancetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Boolean call() throws Exception {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();
		log.debug("Hostname {} - Start Discovery", hostname);

		// Get the connectors previously created/selected in the DetectionOperation
		// strategy
		final IHostMonitoring hostMonitoring = strategyConfig.getHostMonitoring();

		// Get the Host monitor
		final Map<String, Monitor> hosts = hostMonitoring.selectFromType(MonitorType.HOST);

		if (hosts == null) {
			log.error("Hostname {} - No hosts found. Stopping discovery operation.", hostname);
			return false;
		}

		final Monitor hostMonitor = hosts.values().stream().findFirst().orElse(null);
		if (hostMonitor == null) {
			log.error("Hostname {} - No host monitor found. Stop discovery operation", hostname);
			return false;
		}

		// Set the discovery time for the host.
		// The missing monitor detection will set the host as present since its
		// discovery time is the same as the current strategy time
		hostMonitor.setDiscoveryTime(strategyTime);

		final Map<String, Monitor> connectorMonitors = hostMonitoring.selectFromType(MonitorType.CONNECTOR);

		if (connectorMonitors == null || connectorMonitors.isEmpty()) {
			log.error("Hostname {} - No connectors detected in the detection operation. Stopping discovery operation.", hostname);
			return false;
		}

		final Set<String> detectedConnectorFileNames = connectorMonitors
				.values()
				.stream()
				.map(monitor -> monitor.getMetadata(COMPILED_FILE_NAME))
				.collect(Collectors.toSet());

		// Keep only detected/selected connectors, in the store they are indexed by the compiled file name
		// Create a list with connectors defining enclosures on the top since we need to
		// discover the enclosures first
		final List<Connector> connectors = store
				.getConnectors()
				.entrySet()
				.stream()
				.filter(entry -> detectedConnectorFileNames.contains(entry.getKey()))
				.map(Entry::getValue)
				.sorted(new EnclosureFirstComparator())
				.collect(Collectors.toList());

		connectors.stream()
		.filter(connector -> super.validateHardwareMonitors(connector, hostname, NO_HW_MONITORS_FOUND_MSG))
		.forEach(connector -> discover(connector, hostMonitoring, hostname, hostMonitor));

		return true;
	}

	/**
	 * Run the discovery for the given connector
	 *
	 * @param connector      The connector we wish to interpret and discover
	 * @param hostMonitoring The monitors container, it also wraps the {@link SourceTable} objects
	 * @param hostname       The system hostname
	 * @param hostMonitor  The main {@link MonitorType#HOST} monitor detected in the {@link DetectionOperation} strategy.
	 */
	void discover(final Connector connector, final IHostMonitoring hostMonitoring, final String hostname,
			final Monitor hostMonitor) {

		log.debug("Hostname {} - Processing connector {}.", hostname, connector.getCompiledFilename());

		// Perform discovery for the hardware monitor jobs
		// The discovery order is the following: Enclosure, Blade, DiskController, CPU then the rest
		// The order is important so that each monitor can be attached to its parent correctly
		// Start the discovery of the first order in sequential mode
		connector
			.getHardwareMonitors()
			.stream()
			.sorted(new HardwareMonitorComparator())
			.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
				&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
				&& HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
			.forEach(hardwareMonitor -> discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring,
				hostMonitor, hostname));


		final Stream<HardwareMonitor> hardwareMonitors = connector
				.getHardwareMonitors()
				.stream()
				.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor)
						&& validateHardwareMonitorFields(hardwareMonitor, connector.getCompiledFilename(), hostname)
						&& !HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()));

		// The user may want to run queries sent to the host one by one instead of everything in parallel
		if (strategyConfig.getEngineConfiguration().isSequential()) {

			log.info("Hostname {} - Running discovery in sequential mode. Connector: {}.", hostname, connector.getCompiledFilename());

			hardwareMonitors.forEach(hardwareMonitor -> discoverSameTypeMonitors(hardwareMonitor, connector,
					hostMonitoring, hostMonitor, hostname));

		} else {

			log.info("Hostname {} - Running discovery in parallel mode. Connector: {}.", hostname, connector.getCompiledFilename());

			// Now discover the rest of the monitors in parallel mode
			final ExecutorService threadsPool = Executors.newFixedThreadPool(MAX_THREADS_COUNT);

			hardwareMonitors
				.forEach(hardwareMonitor ->
					threadsPool.execute(
							() -> discoverSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostMonitor, hostname)
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
				log.debug("Hostname {} - Waiting for threads' termination aborted with an error.", hostname, e);
			}
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
	 * @param hostMonitor     The host monitor (main)
	 * @param hostname        The user's configured hostname
	 */
	void discoverSameTypeMonitors(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final Monitor hostMonitor, final String hostname) {

		// Process all the sources with their computes
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
				hostMonitor,
				hardwareMonitor.getType(),
				hostname);

	}

	/**
	 * Creates monitors from the same type.
	 * Most of time, this method is called with a {@link SourceInstanceTable};
	 * in that case we loop over all the rows referenced in the {@link InstanceTable},
	 * and for each row we create a new {@link Monitor} instance,
	 * then we set the discovery metadata on each created monitor.
	 *
	 * @param connectorName			The unique name of the {@link Connector}. The compiled file name.
	 * @param hostMonitoring		The {@link IHostMonitoring} instance wrapping source tables and monitors.
	 * @param instanceTable			Defines the source key or the hard coded key.
	 * @param parameters			The discovery parameters to process (from the connector).
	 * @param hostMonitor			The main monitor with {@link MonitorType#HOST} type.
	 * @param monitorType			The current type of the monitor, {@link MonitorType}.
	 * @param hostname				The user's configured hostname used for debug purpose.
	 */
	void createSameTypeMonitors(final String connectorName, final IHostMonitoring hostMonitoring,
								final InstanceTable instanceTable, final Map<String, String> parameters,
								final Monitor hostMonitor, final MonitorType monitorType, final String hostname) {

		// Check the instanceTable, so that, we can create the monitor later
		if (instanceTable == null) {
			log.warn("Hostname {} - No instance tables found with {} during the discovery for the connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
			return;
		}

		// Check discovery parameters, so we can create the monitor with the metadata
		if (parameters == null || parameters.isEmpty()) {
			log.warn("Hostname {} - No parameters found with {} during the discovery for the connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
			return;
		}

		final HostType hostType = strategyConfig.getEngineConfiguration().getHost().getType();

		// Process the instance table
		if (instanceTable instanceof SourceInstanceTable) {

			final SourceInstanceTable sourceInstanceTable = (SourceInstanceTable) instanceTable;
			final String sourceKey = sourceInstanceTable.getSourceKey();

			// No sourceKey no monitor
			if (sourceKey == null) {
				log.error(
						"Hostname {} - No source keys found with monitor {} for connector {}.",
						hostname, monitorType.getNameInConnector(), connectorName);
				return;
			}

			final SourceTable sourceTable = hostMonitoring
					.getConnectorNamespace(connectorName)
					.getSourceTable(sourceKey);

			// No sourceTable no monitor
			if (sourceTable == null) {
				log.debug(
						"Hostname {} - No source tables created with source key {} for connector {}.",
						hostname, sourceKey, connectorName);
				return;
			}

			int idCount = 0;
			// Loop over each row (List) and create one monitor per row
			for (final List<String> row : sourceTable.getTable()) {

				final Monitor monitor = Monitor
						.builder()
						.discoveryTime(strategyTime)
						.build();


				processSourceTableMetadata(connectorName, parameters, sourceKey, row, monitor, idCount);

				setIdentifyingInformation(monitor);

				final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
						.builder()
						.monitor(monitor)
						.hostMonitor(hostMonitor)
						.connectorName(connectorName)
						.hostMonitoring(hostMonitoring)
						.monitorType(monitorType)
						.hostname(hostname)
						.hostType(hostType)
						.build();

				monitorType.getMetaMonitor().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));

				idCount++;
			}

		} else {

			final Monitor monitor = Monitor
					.builder()
					.discoveryTime(strategyTime)
					.build();


			processTextParameters(parameters, monitor, connectorName);

			final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
					.builder()
					.monitor(monitor)
					.hostMonitor(hostMonitor)
					.connectorName(connectorName)
					.hostMonitoring(hostMonitoring)
					.monitorType(monitorType)
					.hostname(hostname)
					.hostType(hostType)
					.build();

			monitorType.getMetaMonitor().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));
		}
	}

	/**
	 * Set the metadata for identifying information using the additional information (1, 2 and 3)
	 * defined in the InstanceTable
	 *
	 * @param monitor       The monitor on which we want to set the identifyingInformation metadata as metadata
	 */
	void setIdentifyingInformation(final Monitor monitor) {

		monitor.addMetadata(IDENTIFYING_INFORMATION,
				MonitorNameBuilder.joinWords(new String[] {
						monitor.getMetadata(ADDITIONAL_INFORMATION1),
						monitor.getMetadata(ADDITIONAL_INFORMATION2),
						monitor.getMetadata(ADDITIONAL_INFORMATION3)
				})
		);
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
		monitor.addMetadata(ID_COUNT, String.valueOf(0));

		// Add the connector name to the metadata
		monitor.addMetadata(CONNECTOR, connectorName);
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
	void processSourceTableMetadata(final String connectorName, final Map<String, String> parameters,
									final String sourceKey, final List<String> row, final Monitor monitor, final int idCount) {

		final String hostname = strategyConfig.getEngineConfiguration().getHost().getHostname();

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
					String extractedValue = row.get(columnIndex);
					if (extractedValue != null) {
						monitor.addMetadata(key, extractedValue.trim());
					}

				} else {
					log.warn("Hostname {} - Column {} does not match the instance table source {} for connector {}.", columnIndex,
							hostname, sourceKey, connectorName);
				}
			} else {
				// Hard coded value
				monitor.addMetadata(key, value);
			}

		}

		// Add the idCount metadata
		monitor.addMetadata(ID_COUNT, String.valueOf(idCount));

		// Add the connector name to the metadata
		monitor.addMetadata(CONNECTOR, connectorName);
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
			log.warn("Hostname {} - No monitor types specified for hardware monitor job with connector {}.", hostname, connectorName);
			return false;
		}

		if (hardwareMonitor.getDiscovery() == null) {
			log.warn("Hostname {} - No {} monitor jobs specified during the discovery for the connector {}.",
					hostname, monitorType.getNameInConnector(), connectorName);
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

		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			log.debug(
					"Hostname {} - Could not detect cpu temperature sensors. isCpuSensor and averageCpuTemperatureWarning metadata won't be set.",
					strategyConfig.getEngineConfiguration().getHost().getHostname());
			return;
		}

		final Monitor hostMonitor = getHostMonitor(hostMonitoring);

		double cpuTemperatureSensorCount = 0;
		double cpuTemperatureWarningAverage = 0.0;

		// Loop over all the temperature monitors to detect cpu temperature sensors then compute the CPU temperature warning
		for (final Monitor temperatureMonitor : temperatureMonitors.values()) {

			final String name = temperatureMonitor.getName();
			final String additionalInformation1 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION1);
			final String additionalInformation2 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION2);
			final String additionalInformation3 = temperatureMonitor.getMetadata(ADDITIONAL_INFORMATION3);
			final Double warningThreshold = getTemperatureWarningThreshold(temperatureMonitor.getMetadata());

			// Is this a cpu sensor? check all the information name, additional information and the warning threshold
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
			hostMonitor.addMetadata(AVERAGE_CPU_TEMPERATURE_WARNING, String.valueOf(cpuTemperatureWarningAverage));
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
				&& ArrayHelper.anyMatchLowerCase(DiscoveryOperation::matchesCpuSensor, data);
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

		hostMonitoring
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> monitor.getMonitorType().getMetaMonitor().hasPresentParameter())
			.filter(monitor -> !strategyTime.equals(monitor.getDiscoveryTime()))
			.forEach(Monitor::setAsMissing);

	}


	@Override
	public void release() {
		// Not implemented yet
	}

}
