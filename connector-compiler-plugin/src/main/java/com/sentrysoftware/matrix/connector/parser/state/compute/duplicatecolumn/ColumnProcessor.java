package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ColumnProcessor extends DuplicateColumnProcessor {

	private static final Pattern COLUMN_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.column\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), "Invalid key: " + key + ConnectorParserConstants.DOT);

		Source source = getSource(matcher, connector);

		DuplicateColumn duplicateColumn = getDuplicateColumn(source, getComputeIndex(matcher));
		notNull(duplicateColumn,
			"Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		String strippedValue = value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1");
		isTrue(strippedValue.matches("\\d+"), "Column number is invalid: " + value);

		duplicateColumn.setColumn(Integer.parseInt(strippedValue));
	}
}
