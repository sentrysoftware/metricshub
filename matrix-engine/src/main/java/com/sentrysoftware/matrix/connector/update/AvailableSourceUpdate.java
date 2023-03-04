package com.sentrysoftware.matrix.connector.update;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class AvailableSourceUpdate extends AbstractConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		Set<Class <? extends Source>> sourceTypes = new HashSet<>();

		for (MonitorJob monitor : connector.getMonitors().values()) {
			if (monitor instanceof StandardMonitorJob standardMonitorJob) {
				standardMonitorJob
					.getDiscovery()
					.getSources()
					.values()
					.forEach(source -> sourceTypes.add(source.getClass()));

				standardMonitorJob
					.getCollect()
					.getSources()
					.values()
					.forEach(source -> sourceTypes.add(source.getClass()));
			}

			if (monitor instanceof AllAtOnceMonitorJob allAtOnceMonitorJob) {
				allAtOnceMonitorJob
					.getAllAtOnce()
					.getSources()
					.values()
					.forEach(source -> sourceTypes.add(source.getClass()));
			}

		connector.getPre().values().forEach(source -> sourceTypes.add(source.getClass()));

		connector.setSourceTypes(sourceTypes);
		}
	}
}
