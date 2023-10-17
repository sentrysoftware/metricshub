package com.sentrysoftware.metricshub.converter.state.computes.add;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorAddProperty {

	private static final String HDF_TYPE_VALUE = "add";
	private static final String YAML_TYPE_VALUE = "add";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE), new ColumnProcessor(), new AddProcessor())
			.collect(Collectors.toSet());
	}
}
