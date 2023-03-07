package com.sentrysoftware.matrix.connector.update;

import java.util.Map;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.MonoCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.MultiCollect;

public class MonitorTaskSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {

		for (Map.Entry<String, MonitorJob> entry : connector.getMonitors().entrySet()) {
//			String key = entry.getKey();
			MonitorJob job = entry.getValue();
			if (job instanceof StandardMonitorJob standardMonitor) {
				Discovery discovery = standardMonitor.getDiscovery();
				discovery.setSourceDep(updateSourceDependency(discovery.getSources()));

				if (standardMonitor.getCollect() instanceof MonoCollect monoCollect) {
					monoCollect.setSourceDep(updateSourceDependency(monoCollect.getSources()));
				}

				if (standardMonitor.getCollect() instanceof MultiCollect multiCollect) {
					multiCollect.setSourceDep(updateSourceDependency(multiCollect.getSources()));
				}
			}

			if (job instanceof AllAtOnceMonitorJob allAtOnceMonitor) {
				AllAtOnce allAtOnce = allAtOnceMonitor.getAllAtOnce();
				allAtOnce.setSourceDep(updateSourceDependency(allAtOnce.getSources()));
			}
		}
	}
}