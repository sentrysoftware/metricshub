package com.sentrysoftware.metricshub.engine.connector.update;

import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import java.util.Map;
import java.util.regex.Pattern;

public class PreSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		final Map<String, Source> sources = connector.getPre();
		if (sources != null) {
			connector.setPreSourceDep(
				updateSourceDependency(
					sources,
					Pattern.compile(
						String.format("\\s*(\\$\\{source::((?i)pre)\\.(%s)\\})\\s*", getSourceIdentifiersRegex(sources)),
						Pattern.MULTILINE
					),
					3
				)
			);
		}
	}
}