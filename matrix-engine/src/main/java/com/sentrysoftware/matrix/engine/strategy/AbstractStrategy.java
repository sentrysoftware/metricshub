package com.sentrysoftware.matrix.engine.strategy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.source.SourceUpdaterVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.compute.ComputeVisitor;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractStrategy implements IStrategy {

	@Autowired
	protected ConnectorStore store;

	@Autowired
	protected StrategyConfig strategyConfig;

	@Autowired
	protected SourceVisitor sourceVisitor;

	@Autowired
	@Setter
	protected Long strategyTime;

	@Override
	public void prepare() {

	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 * 
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The hostname of the target only used for logging
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname) {

		processSourcesAndComputes(sources, hostMonitoring, connector, monitorType, hostname, null);
	}

	/**
	 * Execute each source in the given list of sources then for each source table apply all the attached computes.
	 * When the {@link SourceTable} is ready it is added to {@link HostMonitoring}
	 * 
	 * @param sources        The {@link List} of {@link Source} instances we wish to execute
	 * @param hostMonitoring The {@link SourceTable} and {@link Monitor} container (Namespace)
	 * @param connector      The connector we currently process
	 * @param monitorType    The type of the monitor {@link MonitorType} only used for logging
	 * @param hostname       The hostname of the target only used for logging
	 * @param monitor        The monitor used in the mono instance processing
	 */
	public void processSourcesAndComputes(final List<Source> sources, final IHostMonitoring hostMonitoring,
			final Connector connector, final MonitorType monitorType,
			final String hostname, final Monitor monitor) {
	
		if (sources == null || sources.isEmpty()) {
			log.debug("No source found from connector {} with monitor {}. System {}", connector.getCompiledFilename(), monitorType, hostname);
			return;
		}
	
		// Loop over all the sources and accept the SourceVisitor which is going to
		// visit and process the source
		for (final Source source : sources) {
	
			final SourceTable sourceTable = source.accept(new SourceUpdaterVisitor(sourceVisitor, connector, monitor));
	
			if (sourceTable == null) {
				log.warn("Received null source table for source key {}. Connector {}. Monitor {}. System {}",
						source.getKey(),
						connector.getCompiledFilename(),
						monitorType,
						hostname);
				continue;
			}
	
			hostMonitoring.addSourceTable(source.getKey(), sourceTable);
	
			final List<Compute> computes = source.getComputes();
	
			if (computes != null) {
	
				final ComputeVisitor computeVisitor = new ComputeVisitor(sourceTable, connector);
	
				for (final Compute compute : computes) {
					compute.accept(computeVisitor);
				}
	
				hostMonitoring.addSourceTable(source.getKey(), computeVisitor.getSourceTable());
			}
		}
	}

	/**
	 * Return <code>true</code> if the {@link List} of the {@link HardwareMonitor} instances is not null and not empty in the given
	 * {@link Connector}
	 * 
	 * @param connector The connector we wish to check
	 * @param hostname  The system hostname used for debug purpose
	 * @return boolean value
	 */
	public boolean validateHardwareMonitors(final Connector connector, final String hostname, final String logMessageTemplate) {
		if (connector.getHardwareMonitors() == null || connector.getHardwareMonitors().isEmpty()) {
			log.warn(logMessageTemplate, hostname, connector.getCompiledFilename());
			return false;
		}
	
		return true;
	}
}
