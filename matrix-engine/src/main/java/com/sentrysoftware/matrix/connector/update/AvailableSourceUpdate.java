package com.sentrysoftware.matrix.connector.update;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AbstractCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class AvailableSourceUpdate extends AbstractConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) { // NOSONAR
		Set<Class <? extends Source>> sourceTypes = new HashSet<>();

		for (MonitorJob monitor : connector.getMonitors().values()) {
			if (monitor instanceof StandardMonitorJob standardMonitorJob) {
				processDiscoverySources(sourceTypes, standardMonitorJob.getDiscovery());

				processCollectSources(sourceTypes, standardMonitorJob.getCollect());

			}

			if (monitor instanceof AllAtOnceMonitorJob allAtOnceMonitorJob) {
				final AllAtOnce allAtOnce = allAtOnceMonitorJob.getAllAtOnce();
				processAllAtOnceSources(sourceTypes, allAtOnce);

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
	 * @param allAtOnce {@link AllAtOnce} source job
	 */
	private void processAllAtOnceSources(
		final Set<Class<? extends Source>> sourceTypes,
		final AllAtOnce allAtOnce
	) {
		if (allAtOnce != null) {
			allAtOnce
				.getSources()
				.values()
				.forEach(source -> sourceTypes.add(source.getClass()));
		}
	}

	/**
	 * Process collect job sources
	 * 
	 * @param sourceTypes Types of the sources to be updated
	 * @param collect {@link AbstractCollect} source job
	 */
	private void processCollectSources(
		final Set<Class<? extends Source>> sourceTypes,
		final AbstractCollect collect
	) {
		if (collect != null) {
			collect
				.getSources()
				.values()
				.forEach(source -> sourceTypes.add(source.getClass()));
		}
	}

	/**
	 * Process discovery job sources
	 * 
	 * @param sourceTypes Types of the sources to be updated
	 * @param discovery {@link Discovery} source job
	 */
	private void processDiscoverySources(
		final Set<Class<? extends Source>> sourceTypes,
		final Discovery discovery
	) {
		if (discovery != null) {
			discovery
				.getSources()
				.values()
				.forEach(source -> sourceTypes.add(source.getClass()));
		}
	}
}
