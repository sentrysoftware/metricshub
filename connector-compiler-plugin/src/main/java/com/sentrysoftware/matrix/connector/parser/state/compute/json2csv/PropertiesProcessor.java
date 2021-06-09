package com.sentrysoftware.matrix.connector.parser.state.compute.json2csv;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.SEMICOLON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;

public class PropertiesProcessor extends Json2CSVProcessor {
	private static final Pattern PROPERTIES_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.properties\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return PROPERTIES_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		List<String> selectColumns = new ArrayList<>();

		try {
			Arrays.stream(value.split(SEMICOLON))
			.forEach(selectColumn -> selectColumns.add(selectColumn.trim()));
		} catch (NumberFormatException e) {
			throw new IllegalStateException(
					"PropertiesProcessor parse: Could not select properties from Source ("
							+ value
							+ "): "
							+ e.getMessage());
		}

		((Json2CSV) getCompute(key, connector)).setProperties(selectColumns);
	}
}
