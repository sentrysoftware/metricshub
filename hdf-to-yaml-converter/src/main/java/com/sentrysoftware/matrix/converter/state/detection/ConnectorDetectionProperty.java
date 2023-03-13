package com.sentrysoftware.matrix.converter.state.detection;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.http.ConnectorHttpProperty;

public class ConnectorDetectionProperty {

	private ConnectorDetectionProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(ConnectorHttpProperty.getConnectorProperties())
				.flatMap(Set::stream)
				.collect(Collectors.toSet());
	}
}
