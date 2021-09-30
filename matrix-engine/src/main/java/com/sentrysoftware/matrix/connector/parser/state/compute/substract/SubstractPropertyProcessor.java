package com.sentrysoftware.matrix.connector.parser.state.compute.substract;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubstractPropertyProcessor extends SubstractProcessor {

	private static final Pattern SUBSTRACT_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.substract\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return SUBSTRACT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Substract) getCompute(key, connector)).setSubstract(value);
	}
}
