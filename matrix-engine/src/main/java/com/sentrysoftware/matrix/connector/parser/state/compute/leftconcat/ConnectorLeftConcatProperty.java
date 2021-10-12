package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorLeftConcatProperty {

	private ConnectorLeftConcatProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(LeftConcat.class, LeftConcatProcessor.LEFT_CONCAT_TYPE_VALUE),
				new ColumnProcessor(LeftConcat.class, LeftConcatProcessor.LEFT_CONCAT_TYPE_VALUE),
				new StringProcessor())
			.collect(Collectors.toSet());
	}
}
