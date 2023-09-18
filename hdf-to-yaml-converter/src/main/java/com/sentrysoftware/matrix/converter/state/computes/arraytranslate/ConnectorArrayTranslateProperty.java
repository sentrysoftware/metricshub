package com.sentrysoftware.matrix.converter.state.computes.arraytranslate;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.TranslationTableProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorArrayTranslateProperty {

	private static final String HDF_TYPE_VALUE = "ArrayTranslate";
	private static final String YAML_TYPE_VALUE = "arrayTranslate";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ColumnProcessor(),
			new TranslationTableProcessor(),
			new ResultSeparatorProcessor(),
			new ArraySeparatorProcessor()
		);
	}
}
