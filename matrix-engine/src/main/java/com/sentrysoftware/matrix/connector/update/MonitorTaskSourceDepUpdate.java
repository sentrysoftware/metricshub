package com.sentrysoftware.matrix.connector.update;

import java.util.Map;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.AllAtOnceMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.StandardMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.AllAtOnce;
import com.sentrysoftware.matrix.connector.model.monitor.task.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.task.MonoCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.MultiCollect;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class MonitorTaskSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {

		for (Map.Entry<String, MonitorJob> entry : connector.getMonitors().entrySet()) {
			final String jobName = entry.getKey();
			final MonitorJob job = entry.getValue();

			if (job instanceof StandardMonitorJob standardMonitor) {
				final Discovery discovery = standardMonitor.getDiscovery();
				final Map<String, Source> sources = discovery.getSources();
				discovery.setSourceDep(
					updateSourceDependency(
						sources,
						Pattern.compile(
							String.format(
								"\\s*(\\$((?i)monitors)\\.%s\\.((?i)discovery\\.sources)\\.(%s)\\$)\\s*",
								Pattern.quote(jobName),
								getSourceIdentifiersRegex(sources)
							),
							Pattern.MULTILINE
						),
						4
					)
				);

				if (standardMonitor.getCollect() instanceof MonoCollect monoCollect) {
					final Map<String, Source> monoCollectSources = monoCollect.getSources();
					monoCollect.setSourceDep(
						updateSourceDependency(
							monoCollectSources,
							Pattern.compile(
								String.format(
									"\\s*(\\$((?i)monitors)\\.%s\\.((?i)monocollect\\.sources)\\.(%s)\\$)\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(monoCollectSources)
								),
								Pattern.MULTILINE
							),
							4
						)
					);
				}

				if (standardMonitor.getCollect() instanceof MultiCollect multiCollect) {
					final Map<String, Source> multiCollectSources = multiCollect.getSources();
					multiCollect.setSourceDep(
						updateSourceDependency(
							multiCollectSources,
							Pattern.compile(
								String.format(
									"\\s*(\\$((?i)monitors)\\.%s\\.((?i)multicollect\\.sources)\\.(%s)\\$)\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(multiCollectSources)
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
				final Map<String, Source> allAtOnceSources = allAtOnce.getSources();
				allAtOnce.setSourceDep(
					updateSourceDependency(
						allAtOnceSources,
						Pattern.compile(
							String.format(
								"\\s*(\\$((?i)monitors)\\.%s\\.((?i)allatonce\\.sources)\\.(%s)\\$)\\s*",
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