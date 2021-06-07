package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WbemQueryProcessor extends WbemProcessor {

	private static final Pattern WBEM_QUERY_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.wbemquery\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return WBEM_QUERY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((WBEM) getCriterion(key, connector)).setWbemQuery(value);
	}
}
