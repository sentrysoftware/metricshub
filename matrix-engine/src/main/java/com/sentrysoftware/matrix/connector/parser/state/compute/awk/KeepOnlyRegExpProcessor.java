package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;

public class KeepOnlyRegExpProcessor extends AwkProcessor {

	private static final Pattern KEEP_ONLY_REG_EXP_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.keeponlyregexp\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return KEEP_ONLY_REG_EXP_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Awk) getCompute(key, connector)).setKeepOnlyRegExp(value);
	}
}
