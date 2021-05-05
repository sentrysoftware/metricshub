package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMA;
import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ValueListProcessor extends KeepOnlyMatchingLinesProcessor {

	private static final Pattern VALUE_LIST_KEY_PATTERN = Pattern.compile(
			"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.valuelist\\s*$",
			Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return VALUE_LIST_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), "Invalid key: " + key + ConnectorParserConstants.DOT);

		Source source = getSource(matcher, connector);

		KeepOnlyMatchingLines keepOnlyMatchingLines = getKeepOnlyMatchingLines(source, getComputeIndex(matcher));
		notNull(
				keepOnlyMatchingLines,
				"Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT
		);

		keepOnlyMatchingLines
				.setValueList(
						Arrays.asList(
								value
										.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")
										.split(COMA)
						)
				);
	}
}
