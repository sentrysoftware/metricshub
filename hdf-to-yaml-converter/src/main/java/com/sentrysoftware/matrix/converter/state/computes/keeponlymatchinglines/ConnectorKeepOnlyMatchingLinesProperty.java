package com.sentrysoftware.matrix.converter.state.computes.keeponlymatchinglines;

import java.util.Set;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.RegexpProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ValueListProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorKeepOnlyMatchingLinesProperty {

	private static final String HDF_TYPE_VALUE = "KeepOnlyMatchingLines";
	private static final String YAML_TYPE_VALUE = "keepOnlyMatchingLines";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new RegexpProcessor(),
			new ValueListProcessor(),
			new ColumnProcessor()
		);
	}
}