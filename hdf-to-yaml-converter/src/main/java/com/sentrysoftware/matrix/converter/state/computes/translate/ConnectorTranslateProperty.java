package com.sentrysoftware.matrix.converter.state.computes.translate;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.TranslationTableProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorTranslateProperty {

	private static final String HDF_TYPE_VALUE = "Translate";
	private static final String YAML_TYPE_VALUE = "translate";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream.of(
				new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
				new ColumnProcessor(),
				new TranslationTableProcessor()
			)
			.collect(Collectors.toSet());
	}
}