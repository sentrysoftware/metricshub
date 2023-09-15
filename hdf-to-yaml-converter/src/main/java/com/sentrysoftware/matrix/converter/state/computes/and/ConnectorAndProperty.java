package com.sentrysoftware.matrix.converter.state.computes.and;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorAndProperty {

	private static final String HDF_TYPE_VALUE = "And";
	private static final String YAML_TYPE_VALUE = "and";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE), new ColumnProcessor(), new AndProcessor());
	}
}
