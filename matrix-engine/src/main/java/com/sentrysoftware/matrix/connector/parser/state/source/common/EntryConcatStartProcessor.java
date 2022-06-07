package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;

public class EntryConcatStartProcessor extends AbstractExecuteForEachEntry {

	private static final Pattern ENTRY_CONCAT_START_TOKEN_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.entryconcatstart\\s*$",
			Pattern.CASE_INSENSITIVE);

	public EntryConcatStartProcessor(Class<? extends Source> type, String typeValue) {
		super(type, typeValue);
	}

	@Override
	public Matcher getMatcher(String key) {
		return ENTRY_CONCAT_START_TOKEN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		getSource(key, connector).setEntryConcatStart(value);
	}

}