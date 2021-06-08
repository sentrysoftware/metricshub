package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorWbemProperty {

	private ConnectorWbemProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(WBEMSource.class, WbemProcessor.WBEM_TYPE_VALUE),
				new ForceSerializationProcessor(WBEMSource.class, WbemProcessor.WBEM_TYPE_VALUE),
				new WbemQueryProcessor(),
				new WbemNameSpaceProcessor())
			.collect(Collectors.toSet());
	}
}
