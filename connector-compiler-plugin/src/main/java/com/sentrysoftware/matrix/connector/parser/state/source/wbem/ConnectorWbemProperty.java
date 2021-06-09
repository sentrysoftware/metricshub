package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

public class ConnectorWbemProperty {

	private static final String WBEM_TYPE_VALUE = "WBEM";

	private ConnectorWbemProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(WBEMSource.class, WBEM_TYPE_VALUE),
				new ForceSerializationProcessor(WBEMSource.class, WBEM_TYPE_VALUE),
				new WbemQueryProcessor(WBEMSource.class, WBEM_TYPE_VALUE),
				new WbemNamespaceProcessor(WBEMSource.class, WBEM_TYPE_VALUE))
			.collect(Collectors.toSet());
	}
}
