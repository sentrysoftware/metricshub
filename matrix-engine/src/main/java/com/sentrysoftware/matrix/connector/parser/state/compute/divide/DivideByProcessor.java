package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DivideByProcessor extends DivideProcessor {

	private static final Pattern DIVIDE_BY_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.divideby\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return DIVIDE_BY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Divide) getCompute(key, connector)).setDivideBy(value);
	}
}
