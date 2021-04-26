package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
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
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.compute.ComputeVisitor;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiscoveryOperation extends AbstractStrategy {

	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile("^\\s*instancetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

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

		for (Connector connector : connectors) {
			discover(connector, hostMonitoring, hostname, targetMonitor);
		}

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
	protected void discover(final Connector connector, final IHostMonitoring hostMonitoring, final String hostname,
			final Monitor targetMonitor) {

		log.debug("Processing connector {} for system {}", connector.getCompiledFilename(), hostname);

		if (connector.getHardwareMonitors() == null) {
			log.debug("Could not discover system {}. No hardware monitors found in the connector {}", hostname,
					connector.getCompiledFilename());
			return;
		}


		// Perform discovery for the hardware monitor jobs
		// The discovery order is the following: Enclosure, Blade, DiskController, CPU then the rest
		// The order is important so that each monitor can be attached to its parent correctly
		// Start the discovery of the first order in serial mode
		connector
		.getHardwareMonitors()
		.stream()
		.sorted(new HardwareMonitorComparator())
		.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor) && HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
		.forEach(hardwareMonitor -> discoverSameTypeMonitors(
				hardwareMonitor,
				connector.getCompiledFilename(),
				hostMonitoring,
				targetMonitor,
				hostname));

		// Now discover the rest of the monitors in parallel mode
		connector
		.getHardwareMonitors()
		.parallelStream()
		.filter(hardwareMonitor -> Objects.nonNull(hardwareMonitor) && !HardwareMonitorComparator.ORDER.contains(hardwareMonitor.getType()))
		.forEach(hardwareMonitor -> discoverSameTypeMonitors(
				hardwareMonitor,
				connector.getCompiledFilename(),
				hostMonitoring,
				targetMonitor,
				hostname));
	}

	/**
	 * Discover monitors of the same type. This method processes all the sources of
	 * the discovery stage then creates the required monitors
	 * 
	 * @param hardwareMonitor Defines the discovery {@link InstanceTable}, the {@link Source}
	 *                        to process and all the parameters to create
	 * @param connectorName   The unique name of the connector
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping
	 *                        {@link Monitor} and {@link SourceTable} instances
	 * @param targetMonitor   The target monitor (main)
	 * @param hostname        The user's configured hostname
	 */
	protected void discoverSameTypeMonitors(final HardwareMonitor hardwareMonitor, final String connectorName,
			final IHostMonitoring hostMonitoring, final Monitor targetMonitor, final String hostname) {

		// Is there any discovery job here ?
		final MonitorType monitorType = hardwareMonitor.getType();
		if (monitorType == null) {
			log.warn("No type specified for hardware monitor job with connector {} on system {}", connectorName, hostname);
			return;
		}

		if (hardwareMonitor.getDiscovery() == null) {
			log.warn("No {} monitor job specified during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return;
		}

		// Get the instanceTable, so that, we can create the enclosure monitor
		final InstanceTable instanceTable = hardwareMonitor.getDiscovery().getInstanceTable();
		if (instanceTable == null) {
			log.warn("No instance table found with {} during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return;
		}

		// Get the discovery parameters, so we can create the  monitor using the defined parameters
		final Map<String, String> parameters = hardwareMonitor.getDiscovery().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("No parameter found with {} during the discovery for the connector {} on system {}",
					monitorType.getName(), connectorName, hostname);
			return;
		}

		// Get the sources of the current discovery job
		final List<Source> sources = hardwareMonitor.getDiscovery().getSources();

		// Process all the sources with theirs computes
		processSourcesAndComputes(sources, hostMonitoring, connectorName, monitorType, hostname);

		// Create the monitors
		createSameTypeMonitors(
				connectorName,
				hostMonitoring,
				instanceTable,
				parameters,
				targetMonitor,
				monitorType,
				hostname);

	}

	/**
	 * Create monitors from the same type, most of time this method is called with a {@link SourceInstanceTable},
	 * In that case we loop over all the rows referenced in the {@link InstanceTable}, for each row we create a new
	 * {@link Monitor} instance then we set the discovery parameters on each created monitor
	 * 
	 * @param connectorName  The unique name of the {@link Connector}. The compiled file name
	 * @param hostMonitoring The {@link IHostMonitoring} instance wrapping source tables and monitors
	 * @param instanceTable  Defines the source key or the hard coded key
	 * @param parameters     The discovery parameters to process
	 * @param targetMonitor  The main monitor with {@link MonitorType#TARGET} type
	 * @param monitorType    The current type of the monitor, {@link MonitorType}
	 * @param hostname       The user's configured hostname used for debug purpose
	 */
	protected void createSameTypeMonitors(final String connectorName, final IHostMonitoring hostMonitoring,
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

				monitorType.getConcreteType().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));

				idCount++;
			}

		} else {
			final Monitor monitor = Monitor.builder().build();

			processTextParameters(parameters, monitor);

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

			monitorType.getConcreteType().accept(new MonitorDiscoveryVisitor(monitorBuildingInfo));
		}
	}

	/**
	 * Process the parameters defined in a {@link TextInstanceTable}. I.e. Hard
	 * coded instance table
	 * 
	 * @param parameters The parameters we which to process
	 * @param monitor    The monitor on which we want to set the parameter values
	 */
	protected void processTextParameters(final Map<String, String> parameters, final Monitor monitor) {
		for (final Entry<String, String> parameter : parameters.entrySet()) {

			monitor.addParameter(buildTextParam(parameter.getKey(), parameter.getValue()));

			monitor.addParameter(buildTextParam(HardwareConstants.ID_COUNT_PARAMETER, String.valueOf(0)));

		}
	}

	/**
	 * Process the given row to create discovery parameters on the {@link Monitor} instance
	 * 
	 * @param connectorName The connector unique name used for logging purpose
	 * @param parameters    Lookup of parameter name to value used to create {@link TextParam}
	 * @param sourceKey     The unique identifier of the source used for logging purpose
	 * @param row           The row from the {@link SourceTable} so that we can extract the value when
	 * 						we encounter the pattern `InstanceTable.Column($number)`
	 * @param monitor       The monitor on which we are going to set the parameters
	 * @param idCount		The id used to identify the monitor by its position in {@link SourceTable} lines
	 */
	protected void processSourceTableParameters(final String connectorName, final Map<String, String> parameters,
			final String sourceKey, final List<String> row, final Monitor monitor, final int idCount) {

		// Loop over all the parameters defined in the connector's Instance and create TextParam for each parameter
		for (final Entry<String, String> parameter : parameters.entrySet()) {

			final String parameterName = parameter.getKey();
			final String value = parameter.getValue();
			final Matcher matcher = INSTANCE_TABLE_PATTERN.matcher(value);

			final TextParam textParam;

			// Means we have a column number so we can extract the value from the row
			if (matcher.find()) {
				final Integer columnIndex = Integer.parseInt(matcher.group(1));

				if (columnIndex <= row.size()) {
					textParam = buildTextParam(parameterName, row.get(columnIndex - 1));

				} else {
					log.warn("column {} doesn't match the instance table source {} for connector {}", columnIndex - 1,
							sourceKey, connectorName);
					continue;
				}
			} else {
				// Hard coded value
				textParam = buildTextParam(parameterName, value);
			}

			monitor.addParameter(textParam);
		}

		// Add the idCount parameter
		monitor.addParameter(buildTextParam(HardwareConstants.ID_COUNT_PARAMETER, String.valueOf(idCount)));

	}

	/**
	 * Build a {@link TextParam} instance
	 * 
	 * @param parameterName The name of the parameter we wish to build
	 * @param value         The value of the parameter we wish set in the {@link TextParam} instance
	 * @return {@link TextParam} instance
	 */
	private TextParam buildTextParam(final String parameterName, final String value) {
		final TextParam textParam;
		textParam = TextParam
				.builder()
				.name(parameterName)
				.collectTime(strategyTime)
				.value(value)
				.build();
		return textParam;
	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 * 
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connectorName  The unique name of the connector only used for logging
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The hostname of the target only used for logging
	 */
	protected void processSourcesAndComputes(final List<Source> sources,
			final IHostMonitoring hostMonitoring,
			final String connectorName,
			final MonitorType monitorType,
			final String hostname) {

		if (null == sources || sources.isEmpty()) {
			log.debug("No source found from connector {} with monitor {}. System {}", connectorName, monitorType, hostname);
			return;
		}

		// Loop over all the sources and accept the SourceVisitor which is going to
		// visit and process the source
		for (final Source source : sources) {

			final SourceTable sourceTable = source.accept(sourceVisitor);

			hostMonitoring.addSourceTable(source.getKey(), sourceTable);

			final List<Compute> computes = source.getComputes();

			if (computes != null) {

				final ComputeVisitor computeVisitor = new ComputeVisitor(sourceTable);

				for (final Compute compute : computes) {
					compute.accept(computeVisitor);
				}

				hostMonitoring.addSourceTable(source.getKey(), computeVisitor.getSourceTable());
			}
		}
	}

	@Override
	public void release() {
		// Not implemented yet
	}

	@Override
	public void post() {
		// Not implemented yet
	}

}
