package com.sentrysoftware.matrix.connector.parser.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

public class UrlProcessor extends HttpProcessor {

	private static final Pattern URL_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.url\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return URL_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((HTTPSource) getSource(key, connector)).setUrl(value);
	}
}
