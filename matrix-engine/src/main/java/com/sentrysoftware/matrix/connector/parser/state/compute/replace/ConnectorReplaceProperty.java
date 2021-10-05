package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorReplaceProperty {

	private ConnectorReplaceProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Replace.class, ReplaceProcessor.REPLACE_TYPE_VALUE),
				new ColumnProcessor(Replace.class, ReplaceProcessor.REPLACE_TYPE_VALUE),
				new ReplacePropertyProcessor(),
				new ReplaceByProcessor())
			.collect(Collectors.toSet());
	}
}
