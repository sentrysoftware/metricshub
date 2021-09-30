package com.sentrysoftware.matrix.connector.parser.state.source.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

public class EntryConcatMethodProcessor extends HttpProcessor {

	private static final Pattern ENTRY_CONCAT_METHOD_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.entryconcatmethod\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return ENTRY_CONCAT_METHOD_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((HTTPSource) getSource(key, connector)).setEntryConcatMethod(EntryConcatMethod.getByName(value));
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
					"EntryConcatMethodProcessor parse: could not instantiate EntryConcatMethod from Source ("
							+ value
							+ "): "
							+ e.getMessage());
		}
	}
}
