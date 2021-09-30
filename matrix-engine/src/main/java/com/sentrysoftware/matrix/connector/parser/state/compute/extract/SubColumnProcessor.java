package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

public class SubColumnProcessor extends ExtractProcessor {

	private static final Pattern SUB_COLUMN_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.subcolumn\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return SUB_COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		isTrue(value.matches(ConnectorParserConstants.INTEGER_REGEX), () -> "Invalid sub-column: " + value);

		((Extract) getCompute(key, connector)).setSubColumn(Integer.parseInt(value));
	}
}
