package com.sentrysoftware.matrix.converter.state.computes.json2csv;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.PropertiesProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.SeparatorProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorJson2CsvProperty {

	private static final String HDF_TYPE_VALUE = "Json2Csv";
	private static final String YAML_TYPE_VALUE = "json2csv";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new EntryKeyProcessor(),
			new PropertiesProcessor(),
			new SeparatorProcessor()
		);
	}
}
