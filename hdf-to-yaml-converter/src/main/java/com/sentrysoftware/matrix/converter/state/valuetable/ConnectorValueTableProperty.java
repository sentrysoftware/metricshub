package com.sentrysoftware.matrix.converter.state.valuetable;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorValueTableProperty {

	private ConnectorValueTableProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream.of(new ValueTableProcessor(), new CollectParameterProcessor()).collect(Collectors.toSet());
	}
}
