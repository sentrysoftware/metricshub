package com.sentrysoftware.matrix.converter.state.detection.ucs;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class ConnectorUcsProperty {

	private static final String HDF_TYPE_VALUE = "UCS";
	private static final String YAML_TYPE_VALUE = "ucs";

	private ConnectorUcsProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new QueryProcessor(),
			new ExpectedResultProcessor(),
			new ForceSerializationProcessor()
		)
		.collect(Collectors.toSet());
	}
}