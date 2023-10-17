package com.sentrysoftware.metricshub.converter.state.computes.awk;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorAwkProperty {

	private static final String HDF_TYPE_VALUE = "Awk";
	private static final String YAML_TYPE_VALUE = "awk";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
				new AwkScriptProcessor(),
				new KeepOnlyRegExpProcessor(),
				new ExcludeRegExpProcessor(),
				new SelectColumnsProcessor(),
				new SeparatorsProcessor()
			)
			.collect(Collectors.toSet());
	}
}
