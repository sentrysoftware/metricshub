package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class DivideByProcessor extends DivideProcessor {

	private static final Pattern DIVIDE_BY_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.divideby\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return DIVIDE_BY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		Divide divide = getCompute(getSource(matcher, connector), getComputeIndex(matcher));
		notNull(divide,
			() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		divide.setDivideBy(value);
	}
}
