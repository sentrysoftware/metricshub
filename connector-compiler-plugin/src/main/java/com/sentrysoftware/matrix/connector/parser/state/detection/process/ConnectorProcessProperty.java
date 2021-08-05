package com.sentrysoftware.matrix.connector.parser.state.detection.process;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorProcessProperty {

	private ConnectorProcessProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(Process.class, ProcessCommandLineProcessor.PROCESS_TYPE_VALUE),
						new ForceSerializationProcessor(Process.class, ProcessCommandLineProcessor.PROCESS_TYPE_VALUE),
						new ProcessCommandLineProcessor())
				.collect(Collectors.toSet());
	}
}
