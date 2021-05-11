package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ReplaceByProcessor extends ReplaceProcessor {

	private static final Pattern REPLACE_BY_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.replaceby\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return REPLACE_BY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		Replace replace = getCompute(getSource(matcher, connector), getComputeIndex(matcher));
		notNull(replace,
			() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		replace.setReplaceBy(value);
	}
}
