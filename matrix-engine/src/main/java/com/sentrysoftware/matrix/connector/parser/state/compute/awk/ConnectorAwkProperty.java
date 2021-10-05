package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorAwkProperty {

	private ConnectorAwkProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(Awk.class, AwkProcessor.AWK_TYPE_VALUE),
						new AwkScriptProcessor(),
						new KeepOnlyRegExpProcessor(),
						new SelectColumnsProcessor(),
						new SeparatorsProcessor())
				.collect(Collectors.toSet());
	}
}
