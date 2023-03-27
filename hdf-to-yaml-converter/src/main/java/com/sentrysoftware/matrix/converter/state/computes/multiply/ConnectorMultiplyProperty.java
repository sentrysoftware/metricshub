package com.sentrysoftware.matrix.converter.state.computes.multiply;

import java.util.Set;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorMultiplyProperty {

	private static final String HDF_TYPE_VALUE = "Multiply";
	private static final String YAML_TYPE_VALUE = "multiply";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
				new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
				new ColumnProcessor(),
				new MultiplyByProcessor()
			);
	}
}