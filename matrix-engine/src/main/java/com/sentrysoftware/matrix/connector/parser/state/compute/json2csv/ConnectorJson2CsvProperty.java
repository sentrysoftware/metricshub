package com.sentrysoftware.matrix.connector.parser.state.compute.json2csv;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2Csv;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

public class ConnectorJson2CsvProperty {
	
	private ConnectorJson2CsvProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(Json2Csv.class, Json2CsvProcessor.JSON_2_CSV_TYPE_VALUE),
						new EntryKeyProcessor(),
						new PropertiesProcessor(),
						new SeparatorProcessor())
				.collect(Collectors.toSet());
	}
}
