package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResultContentProcessor extends HttpProcessor {

	private static final Pattern RESULT_CONTENT_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.resultcontent\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return RESULT_CONTENT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((HTTP) getCriterion(key, connector)).setResultContent(ResultContent.valueOf(value));
	}
}
