package com.sentrysoftware.matrix.connector.parser.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

public class ResultContentProcessor extends HttpProcessor {

	private static final Pattern RESULT_CONTENT_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.resultcontent\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return RESULT_CONTENT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((HTTPSource) getSource(key, connector)).setResultContent(ResultContent.getByName(value));
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
					"ResultContentProcessor parse: could not instantiate ResultContent from Source ("
							+ value
							+ "): "
							+ e.getMessage());
		}
	}
}
