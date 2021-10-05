package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorDivideProperty {

	private ConnectorDivideProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Divide.class, DivideProcessor.DIVIDE_TYPE_VALUE),
				new ColumnProcessor(Divide.class, DivideProcessor.DIVIDE_TYPE_VALUE),
				new DivideByProcessor())
			.collect(Collectors.toSet());
	}
}
