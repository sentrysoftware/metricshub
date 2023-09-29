package com.sentrysoftware.matrix.converter.state.computes.xml2csv;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.computes.common.ComputeTypeProcessor;
import com.sentrysoftware.matrix.converter.state.computes.common.PropertiesProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorXml2CsvProperty {

	private static final String HDF_TYPE_VALUE = "Xml2Csv";
	private static final String YAML_TYPE_VALUE = "xml2csv";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new ComputeTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new RecordTagProcessor(),
			new PropertiesProcessor()
		);
	}
}
