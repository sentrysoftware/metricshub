package com.sentrysoftware.matrix.converter.state.computes.keepcolumns;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorKeepColumnsProperty {

	private static final String HDF_TYPE_VALUE = "KeepColumns";
	private static final String YAML_TYPE_VALUE = "keepColumns";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE), new ColumnNumbersProcessor());
	}
}
