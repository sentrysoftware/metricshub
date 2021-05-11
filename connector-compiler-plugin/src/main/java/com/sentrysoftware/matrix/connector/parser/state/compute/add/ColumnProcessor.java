package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ColumnProcessor extends AddProcessor {

	private static final Pattern COLUMN_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.column\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		Add add = getCompute(getSource(matcher, connector), getComputeIndex(matcher));
		notNull(add,
			() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		isTrue(value.matches("\\d+"), () -> "Column number is invalid: " + value);

		add.setColumn(Integer.parseInt(value));
	}
}
