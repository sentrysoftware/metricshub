package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

public class ConnectorOsProperty {

	private ConnectorOsProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(OS.class, OsProcessor.OS_TYPE_VALUE),
						new ForceSerializationProcessor(OS.class, OsProcessor.OS_TYPE_VALUE),
						new KeepOnlyProcessor(),
						new ExcludeProcessor())
				.collect(Collectors.toSet());
	}
}
