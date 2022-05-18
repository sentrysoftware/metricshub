package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorXml2CsvProperty {

	private ConnectorXml2CsvProperty() { }

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Stream
				.of(
						new TypeProcessor(Xml2Csv.class, Xml2CsvProcessor.XML_2_CSV_TYPE_VALUE),
						new RecordTagProcessor(),
						new PropertiesProcessor())
				.collect(Collectors.toSet());
	}
}
