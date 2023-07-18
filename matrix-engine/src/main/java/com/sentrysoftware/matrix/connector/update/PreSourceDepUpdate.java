package com.sentrysoftware.matrix.connector.update;

import java.util.Map;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

public class PreSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		final Map<String, Source> sources = connector.getPre();
		connector.setPreSourceDep(
			updateSourceDependency(
				sources,
				Pattern.compile(
					String.format(
						"\\s*(\\$\\{source::((?i)pre)\\.(%s)\\})\\s*",
						getSourceIdentifiersRegex(sources)
					),
					Pattern.MULTILINE
				),
				3
			)
		);
	}

}
