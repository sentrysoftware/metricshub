package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorExtractProperty {

	private ConnectorExtractProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Extract.class, ExtractProcessor.EXTRACT_TYPE_VALUE),
				new ColumnProcessor(Extract.class, ExtractProcessor.EXTRACT_TYPE_VALUE),
				new SubColumnProcessor(),
				new SubSeparatorsProcessor())
			.collect(Collectors.toSet());
	}
}
