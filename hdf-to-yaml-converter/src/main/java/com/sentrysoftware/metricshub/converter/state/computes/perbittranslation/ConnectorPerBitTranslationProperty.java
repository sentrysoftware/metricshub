package com.sentrysoftware.metricshub.converter.state.computes.perbittranslation;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.computes.common.ColumnProcessor;
import com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorPerBitTranslationProperty {

	private static final String HDF_TYPE_VALUE = "PerBitTranslation";
	private static final String YAML_TYPE_VALUE = "perBitTranslation";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ColumnProcessor(),
			new BitTranslationTableProcessor(),
			new BitListProcessor()
		);
	}
}
