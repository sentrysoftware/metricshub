package com.sentrysoftware.metricshub.converter.state.computes.extract;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorExtractProperty {

	private static final String HDF_TYPE_VALUE = "Extract";
	private static final String YAML_TYPE_VALUE = "extract";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ColumnProcessor(),
			new SubColumnProcessor(),
			new SubSeparatorsProcessor()
		);
	}
}
