package com.sentrysoftware.matrix.connector.parser.state.compute.keepcolumns;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

public class ColumnNumbersProcessor extends KeepColumnsProcessor {

	private static final Pattern KEEP_COLUMNS_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.columnnumbers\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return KEEP_COLUMNS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		List<Integer> columnNumbers = new ArrayList<>();

		try {

			Arrays.stream(value.split(COMMA))
				.forEach(columnNumber -> columnNumbers.add(Integer.parseInt(columnNumber.trim())));

		} catch (NumberFormatException e) {

			throw new IllegalStateException(
				"ColumnNumbersProcessor parse: invalid column number in "
					+ value
					+ ": "
					+ e.getMessage());
		}

		((KeepColumns) getCompute(key, connector)).setColumnNumbers(columnNumbers);
	}
}
