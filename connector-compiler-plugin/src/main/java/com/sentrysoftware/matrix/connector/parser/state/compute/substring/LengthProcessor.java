package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;

public class LengthProcessor extends SubstringProcessor {

	private static final Pattern LENGTH_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.length\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return LENGTH_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(String key, String value, Connector connector) {

		super.parse(key, value, connector);

		((Substring) getCompute(key, connector)).setLength(value);
	}
}
