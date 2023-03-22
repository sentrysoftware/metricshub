package com.sentrysoftware.matrix.converter.state.computes;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.add.ConnectorAddProperty;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorComputeProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			ConnectorAddProperty.getConnectorProperties()
		)
		.flatMap(Set::stream)
		.collect(Collectors.toSet());
	}
}