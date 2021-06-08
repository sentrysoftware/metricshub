package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WbemQueryProcessor extends WbemProcessor {

	private static final Pattern WBEM_QUERY_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.WbemQuery\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return WBEM_QUERY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((WBEMSource) getSource(key, connector)).setWbemQuery(value);
	}
}
