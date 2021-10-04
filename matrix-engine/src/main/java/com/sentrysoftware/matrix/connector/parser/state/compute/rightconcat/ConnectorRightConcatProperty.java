package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorRightConcatProperty {

	private ConnectorRightConcatProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(RightConcat.class, RightConcatProcessor.RIGHT_CONCAT_TYPE_VALUE),
				new ColumnProcessor(RightConcat.class, RightConcatProcessor.RIGHT_CONCAT_TYPE_VALUE),
				new StringProcessor())
			.collect(Collectors.toSet());
	}
}
