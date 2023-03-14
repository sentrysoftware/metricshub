package com.sentrysoftware.matrix.converter.state.detection.wbem;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.WbemNameSpaceProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.WbemQueryProcessor;

public class ConnectorWbemProperty {

	private static final String WBEM_TYPE_VALUE = "WBEM";

	private ConnectorWbemProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(WBEM_TYPE_VALUE),
					new ForceSerializationProcessor(WBEM_TYPE_VALUE),
					new ExpectedResultProcessor(WBEM_TYPE_VALUE),
					new WbemNameSpaceProcessor(WBEM_TYPE_VALUE),
					new WbemQueryProcessor(WBEM_TYPE_VALUE),
					new ErrorMessageProcessor(WBEM_TYPE_VALUE))
				.collect(Collectors.toSet());
	}
}
