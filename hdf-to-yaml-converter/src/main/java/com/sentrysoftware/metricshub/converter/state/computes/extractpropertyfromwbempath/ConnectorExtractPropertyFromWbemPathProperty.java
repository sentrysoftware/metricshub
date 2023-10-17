package com.sentrysoftware.metricshub.converter.state.computes.extractpropertyfromwbempath;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorExtractPropertyFromWbemPathProperty {

	private static final String HDF_TYPE_VALUE = "ExtractPropertyFromWbemPath";
	private static final String YAML_TYPE_VALUE = "extractPropertyFromWbemPath";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ColumnProcessor(),
			new PropertyProcessor()
		);
	}
}
