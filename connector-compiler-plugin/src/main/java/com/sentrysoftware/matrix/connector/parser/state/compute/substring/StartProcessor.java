package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;

public class StartProcessor extends SubstringProcessor {

	private static final Pattern START_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.start\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return START_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		super.parse(key, value, connector);

		((Substring) getCompute(key, connector)).setStart(value);

	}
}
