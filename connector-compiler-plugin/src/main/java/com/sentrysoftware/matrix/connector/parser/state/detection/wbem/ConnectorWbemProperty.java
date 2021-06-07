package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorWbemProperty {

	private ConnectorWbemProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(WBEM.class, WbemProcessor.WBEM_TYPE_VALUE),
					new ForceSerializationProcessor(WBEM.class, WbemProcessor.WBEM_TYPE_VALUE),
					new ExpectedResultProcessor(WBEM.class, WbemProcessor.WBEM_TYPE_VALUE),
					new WbemNameSpaceProcessor(),
					new WbemQueryProcessor(),
					new ErrorMessageProcessor())
				.collect(Collectors.toSet());
	}
}
