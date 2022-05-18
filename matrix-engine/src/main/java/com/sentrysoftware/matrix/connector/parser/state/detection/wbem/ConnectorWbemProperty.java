package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.Wbem;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.WbemQueryProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorWbemProperty {

	private static final String WBEM_TYPE_VALUE = "WBEM";

	private ConnectorWbemProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(Wbem.class, WBEM_TYPE_VALUE),
					new ForceSerializationProcessor(Wbem.class, WBEM_TYPE_VALUE),
					new ExpectedResultProcessor(Wbem.class, WBEM_TYPE_VALUE),
					new WbemNameSpaceProcessor(Wbem.class, WBEM_TYPE_VALUE),
					new WbemQueryProcessor(Wbem.class, WBEM_TYPE_VALUE),
					new ErrorMessageProcessor(Wbem.class, WBEM_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
