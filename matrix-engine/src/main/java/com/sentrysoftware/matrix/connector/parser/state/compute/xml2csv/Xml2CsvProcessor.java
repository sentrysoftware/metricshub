package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class Xml2CsvProcessor extends AbstractStateParser {

	public static final String XML_2_CSV_TYPE_VALUE = "Xml2Csv";

	@Override
	public Class<Xml2Csv> getType() {
		return Xml2Csv.class;
	}

	@Override
	public String getTypeValue() {
		return XML_2_CSV_TYPE_VALUE;
	}
}
