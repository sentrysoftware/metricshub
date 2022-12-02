package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;

public class AuthenticationTokenProcessor extends HttpProcessor {

	private static final Pattern AUTHENTICATION_TOKEN_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.authenticationtoken\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return AUTHENTICATION_TOKEN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Http) getCriterion(key, connector)).setAuthenticationToken(value);
	}

}
