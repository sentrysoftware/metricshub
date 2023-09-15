package com.sentrysoftware.matrix.converter.state.computes.excludematchinglines;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.RegexpProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ValueListProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorExcludeMatchingLinesProperty {

	private static final String HDF_TYPE_VALUE = "ExcludeMatchingLines";
	private static final String YAML_TYPE_VALUE = "excludeMatchingLines";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new RegexpProcessor(),
			new ValueListProcessor(),
			new ColumnProcessor()
		);
	}
}
