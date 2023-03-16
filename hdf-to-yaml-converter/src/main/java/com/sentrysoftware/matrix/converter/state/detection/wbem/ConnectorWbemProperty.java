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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class ConnectorWbemProperty {

	private static final String WBEM_HDF_TYPE_VALUE = "WBEM";
	private static final String WBEM_YAML_TYPE_VALUE = "wbem";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(WBEM_HDF_TYPE_VALUE, WBEM_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExpectedResultProcessor(),
			new WbemNameSpaceProcessor(),
			new WbemQueryProcessor(),
			new ErrorMessageProcessor()
		)
		.collect(Collectors.toSet());
	}
}
