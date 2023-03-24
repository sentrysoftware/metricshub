package com.sentrysoftware.matrix.converter.state.computes.convert;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorConvertProperty {

	private static final String HDF_TYPE_VALUE = "";
	private static final String YAML_TYPE_VALUE = "";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Collections.emptySet();
	}
}