package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorMultiplyProperty {

	private ConnectorMultiplyProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Multiply.class, MultiplyProcessor.MULTIPLY_TYPE_VALUE),
				new ColumnProcessor(Multiply.class, MultiplyProcessor.MULTIPLY_TYPE_VALUE),
				new MultiplyByProcessor())
			.collect(Collectors.toSet());
	}
}
