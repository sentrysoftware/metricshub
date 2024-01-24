package org.sentrysoftware.metricshub.engine.connector.update;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.SimpleMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractCollect;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Discovery;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Simple;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * Implementation of {@link AbstractConnectorUpdateChain} for updating available sources in the connector.
 */
public class AvailableSourceUpdate extends AbstractConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) { // NOSONAR
		Set<Class<? extends Source>> sourceTypes = new HashSet<>();

		for (MonitorJob monitor : connector.getMonitors().values()) {
			if (monitor instanceof StandardMonitorJob standardMonitorJob) {
				processDiscoverySources(sourceTypes, standardMonitorJob.getDiscovery());

				processCollectSources(sourceTypes, standardMonitorJob.getCollect());
			}

			if (monitor instanceof SimpleMonitorJob simpleMonitorJob) {
				final Simple simple = simpleMonitorJob.getSimple();
				processSimpleSources(sourceTypes, simple);
			}

			final Map<String, Source> pre = connector.getPre();
			if (pre != null) {
				pre.values().forEach(source -> sourceTypes.add(source.getClass()));
			}

			connector.setSourceTypes(sourceTypes);
		}
	}

	/**
	 * Process all at once job sources
	 *
	 * @param sourceTypes Types of the sources to be updated
	 * @param simple {@link Simple} source job
	 */
	private void processSimpleSources(final Set<Class<? extends Source>> sourceTypes, final Simple simple) {
		if (simple != null) {
			simple.getSources().values().forEach(source -> sourceTypes.add(source.getClass()));
		}
	}

	/**
	 * Process collect job sources
	 *
	 * @param sourceTypes Types of the sources to be updated
	 * @param collect {@link AbstractCollect} source job
	 */
	private void processCollectSources(final Set<Class<? extends Source>> sourceTypes, final AbstractCollect collect) {
		if (collect != null) {
			collect.getSources().values().forEach(source -> sourceTypes.add(source.getClass()));
		}
	}

	/**
	 * Process discovery job sources
	 *
	 * @param sourceTypes Types of the sources to be updated
	 * @param discovery {@link Discovery} source job
	 */
	private void processDiscoverySources(final Set<Class<? extends Source>> sourceTypes, final Discovery discovery) {
		if (discovery != null) {
			discovery.getSources().values().forEach(source -> sourceTypes.add(source.getClass()));
		}
	}
}
