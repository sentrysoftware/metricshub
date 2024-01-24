package org.sentrysoftware.metricshub.engine.connector.update;

import java.util.Map;
import java.util.regex.Pattern;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.MonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.SimpleMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.StandardMonitorJob;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.AbstractCollect;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Discovery;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Simple;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

/**
 * Implementation of {@link SourceConnectorUpdateChain} for updating monitor task source dependencies in the connector.
 */
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

			if (job instanceof SimpleMonitorJob simpleMonitorJob) {
				final Simple simple = simpleMonitorJob.getSimple();
				if (simple != null) {
					final Map<String, Source> simpleSources = simple.getSources();
					simple.setSourceDep(
						updateSourceDependency(
							simpleSources,
							Pattern.compile(
								String.format(
									"\\s*(\\$\\{source::((?i)monitors)\\.%s\\.((?i)simple\\.sources)\\.(%s)\\})\\s*",
									Pattern.quote(jobName),
									getSourceIdentifiersRegex(simpleSources)
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
