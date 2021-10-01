package com.sentrysoftware.matrix.connector.parser.state.compute.xml2csv;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Xml2Csv;

public class PropertiesProcessor extends Xml2CsvProcessor {

	private static final Pattern RECORD_TAG_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.Properties\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(final String key) {
		return RECORD_TAG_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Xml2Csv) getCompute(key, connector)).setProperties(value);
	}
}
