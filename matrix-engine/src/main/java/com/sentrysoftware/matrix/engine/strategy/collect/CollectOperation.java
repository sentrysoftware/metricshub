package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.CollectType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.engine.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectOperation extends AbstractStrategy {

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
		for (Connector connector : connectors) {
			collect(connector, hostMonitoring, hostname);
		}

		return true;
	}

	/**
	 * Run the collect for the given connector
	 * 
	 * @param connector      The connector we wish to interpret and collect
	 * @param hostMonitoring The monitors container, it also wraps the {@link SourceTable} objects
	 * @param hostname       The system hostname
	 */
	protected void collect(final Connector connector, final IHostMonitoring hostMonitoring, final String hostname) {
		log.debug("Collect - Processing connector {} for system {}", connector.getCompiledFilename(), hostname);

		if (connector.getHardwareMonitors() == null) {
			log.debug("Collect - Could not collect system {}. No hardware monitors found in the connector {}", hostname,
					connector.getCompiledFilename());
			return;
		}

		// The parallel stream might need to be disabled, i.e. configured by the user
		connector
		.getHardwareMonitors()
		.parallelStream()
		.forEach(hardwareMonitor -> collectSameTypeMonitors(hardwareMonitor, connector, hostMonitoring, hostname));
	}

	/**
	 * Collect monitors of the same type. This method processes all the sources of the collect stage then collect the required monitors
	 * 
	 * @param hardwareMonitor Defines the {@link Collect} valueTable, the {@link Source} to process and all the parameters
	 * @param connector       The connector we currently process
	 * @param hostMonitoring  The {@link IHostMonitoring} instance wrapping {@link Monitor} and {@link SourceTable} instances
	 * @param hostname        The user's configured hostname
	 */
	protected void collectSameTypeMonitors(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final String hostname) {

		// Is there any collect job here ?
		final MonitorType monitorType = hardwareMonitor.getType();
		if (monitorType == null) {
			log.warn("Collect - No type specified for hardware monitor job with connector {} on system {}",
					connector.getCompiledFilename(), hostname);
			return;
		}

		if (hardwareMonitor.getCollect() == null) {
			log.warn("Collect - No {} monitor job specified during the collect for the connector {} on system {}",
					monitorType.getName(), connector.getCompiledFilename(), hostname);
			return;
		}

		// Get the collectType, so that,
		final CollectType collectType = hardwareMonitor.getCollect().getType();
		if (collectType == null) {
			log.warn("Collect - No collect type found with {} during the collect for the connector {} on system {}",
					monitorType.getName(), connector.getCompiledFilename(), hostname);
			return;
		}

		// Get the collect parameters, so we can create the monitor with the metadata
		final Map<String, String> parameters = hardwareMonitor.getCollect().getParameters();
		if (parameters == null || parameters.isEmpty()) {
			log.warn("Collect - No parameter found with {} during the collect for the connector {} on system {}",
					monitorType.getName(), connector.getCompiledFilename(), hostname);
			return;
		}

		// Get the sources of the current collect job
		final List<Source> sources = hardwareMonitor.getCollect().getSources();

		// Process all the sources with theirs computes
		if (CollectType.MULTI_INSTANCE.equals(collectType)) {

			collectMultiInstance(hardwareMonitor,
					connector,
					hostMonitoring,
					monitorType,
					parameters,
					sources,
					hostname);

		} else {
			collectMonoInstance(sources, hostMonitoring, connector, monitorType, parameters, hostname);
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
	private void collectMultiInstance(final HardwareMonitor hardwareMonitor, final Connector connector,
			final IHostMonitoring hostMonitoring, final MonitorType monitorType,
			final Map<String, String> parameters, final List<Source> sources,
			final String hostname) {

		// Run the sources and the computes
		processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname);

		// Process the MultiInstance value table
		processValueTable(hardwareMonitor.getCollect().getValueTable(),
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
	protected void processValueTable(final String valueTable, final String connectorName,
			final IHostMonitoring hostMonitoring, final Map<String, String> parameters,
			final MonitorType monitorType, final String hostname) {

		// No sourceKey no monitor
		if (valueTable == null) {
			log.error("Collect - No valueTable found with monitor {} for connector {} on system {}", 
					monitorType.getName(), connectorName, hostname);
			return;
		}

		// Get the source table used to collect parameters
		final SourceTable sourceTable = hostMonitoring.getSourceTableByKey(valueTable);

		// No sourceTable no monitor
		if (sourceTable == null) {
			log.error("Collect - No source table created with source key {} for connector {} on system {}",
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
	private void collectMonitors(final String valueTable, final SourceTable sourceTable,
			final String connectorName, final IHostMonitoring hostMonitoring,
			final Map<String, String> parameters, final MonitorType monitorType,
			final String hostname) {

		for (final List<String> row : sourceTable.getTable()) {

			Optional<Monitor> monitorOpt = getMonitor(valueTable,
					monitorType,
					hostMonitoring,
					row,
					parameters.get(HardwareConstants.DEVICE_ID));

			if (!monitorOpt.isPresent()) {
				log.warn("Collect - Couldn't find monitor {} associated with row {}. Connector {}",
						monitorType.getName(), row, connectorName);
				continue;
			}

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
			monitorType.getConcreteType().accept(new MonitorCollectVisitor(monitorCollectInfo));

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
	protected Optional<Monitor> getMonitor(final String valueTable, final MonitorType monitorType,
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
			return monitors.
					values()
					.stream()
					.filter(mo -> Objects.nonNull(mo.getMetadata()) &&
							id.equals(mo.getMetadata().get(HardwareConstants.DEVICE_ID)))
					.findFirst();
		}

		return Optional.empty();
	}

	protected void collectMonoInstance(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			Map<String, String> parameters, String hostname) {
		// Not implemented yet
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
