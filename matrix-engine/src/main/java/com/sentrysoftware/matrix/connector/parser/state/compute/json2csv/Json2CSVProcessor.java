package com.sentrysoftware.matrix.connector.parser.state.compute.json2csv;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class Json2CSVProcessor extends AbstractStateParser {

	protected static final String JSON_2_CSV_TYPE_VALUE = "Json2Csv";

	@Override
	public Class<Json2CSV> getType() {
		return Json2CSV.class;
	}

	@Override
	public String getTypeValue() {
		return JSON_2_CSV_TYPE_VALUE;
	}
}
