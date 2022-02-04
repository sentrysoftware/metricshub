package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;

public class EntryConcatMethodProcessor extends AbstractExecuteForEachEntry {

	private static final Pattern ENTRY_CONCAT_METHOD_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.entryconcatmethod\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return ENTRY_CONCAT_METHOD_KEY_PATTERN.matcher(key);
	}

	public EntryConcatMethodProcessor(Class<? extends Source> type, String typeValue) {
		super(type, typeValue);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {

			getSource(key, connector).setEntryConcatMethod(EntryConcatMethod.getByName(value));

		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
					String.format("EntryConcatMethodProcessor parse: could not instantiate EntryConcatMethod."
					+ " Source key (%s). Source value (%s). Error: %s", key, value, e.getMessage()));
		}
	}
}
