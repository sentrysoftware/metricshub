package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ColumnProcessor extends LeftConcatProcessor {

	private static final Pattern COLUMN_KEY_PATTERN = Pattern.compile(
			"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.column\\s*$",
			Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), "Invalid key: " + key + ConnectorParserConstants.DOT);

		LeftConcat leftConcat = getLeftConcat(matcher, connector);
		notNull(
				leftConcat,
				"Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT
		);

		leftConcat
				.setColumn(
						Integer.parseInt(
								value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")
						)
				);
	}
}
