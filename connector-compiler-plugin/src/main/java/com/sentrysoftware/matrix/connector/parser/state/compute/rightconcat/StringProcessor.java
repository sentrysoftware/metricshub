package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

public class StringProcessor extends RightConcatProcessor {

	private static final Pattern REGEXP_KEY_PATTERN = Pattern.compile(
			"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.string\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return REGEXP_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), "Invalid key: " + key + ConnectorParserConstants.DOT);

		RightConcat rightConcat = getCompute(getSource(matcher, connector), getComputeIndex(matcher));
		notNull(rightConcat,
				() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		rightConcat.setString(value);
	}
}
