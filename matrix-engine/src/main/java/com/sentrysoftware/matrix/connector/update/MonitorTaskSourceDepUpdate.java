package com.sentrysoftware.matrix.connector.update;

import java.util.Map;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AbstractCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class MonitorTaskSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {

		for (Map.Entry<String, MonitorJob> entry : connector.getMonitors().entrySet()) {
			final String jobName = entry.getKey();
			final MonitorJob job = entry.getValue();

			if (job instanceof StandardMonitorJob standardMonitorJob) {
				final Discovery discovery = standardMonitorJob.getDiscovery();
				if (discovery != null) {
					final Map<String, Source> sources = discovery.getSources();
					discovery.setSourceDep(
						updateSourceDependency(
							sources,
							Pattern.compile(
								String.format(
									"\\s*(\\$\\{source::((?i)monitors)\\.%s\\.((?i)discovery\\.sources)\\.(%s)\\})\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(sources)
								),
								Pattern.MULTILINE
							),
							4
						)
					);
				}

				final AbstractCollect collect = standardMonitorJob.getCollect();

				if (collect != null) {
					final Map<String, Source> collectSources = collect.getSources();
					collect.setSourceDep(
						updateSourceDependency(
							collectSources,
							Pattern.compile(
								String.format(
									"\\s*(\\$\\{source::((?i)monitors)\\.%s\\.((?i)collect\\.sources)\\.(%s)\\})\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(collectSources)
								),
								Pattern.MULTILINE
							),
							4
						)
					);
				}

			}

			if (job instanceof AllAtOnceMonitorJob allAtOnceMonitor) {
				final AllAtOnce allAtOnce = allAtOnceMonitor.getAllAtOnce();
				if (allAtOnce != null) {
					final Map<String, Source> allAtOnceSources = allAtOnce.getSources();
					allAtOnce.setSourceDep(
						updateSourceDependency(
							allAtOnceSources,
							Pattern.compile(
								String.format(
									"\\s*(\\$\\{source::((?i)monitors)\\.%s\\.((?i)allatonce\\.sources)\\.(%s)\\})\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(allAtOnceSources)
								),
								Pattern.MULTILINE
							),
							4
						)
					);
				}
			}
		}
	}
}