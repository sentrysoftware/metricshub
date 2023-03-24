package com.sentrysoftware.matrix.converter.state.computes.replace;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorReplaceProperty {

	private static final String HDF_TYPE_VALUE = "Replace";
	private static final String YAML_TYPE_VALUE = "replace";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream.of(
				new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
				new ColumnProcessor(),
				new ReplaceProcessor(),
				new ReplaceByProcessor()
			)
			.collect(Collectors.toSet());
	}
}