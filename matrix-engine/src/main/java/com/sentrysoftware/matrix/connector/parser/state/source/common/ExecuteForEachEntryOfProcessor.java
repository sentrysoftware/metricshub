package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

public class ExecuteForEachEntryOfProcessor extends AbstractExecuteForEachEntry {

	private static final Pattern EXECUTE_FOR_EACH_ENTRY_OF_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.executeforeachentryof\\s*$",
			Pattern.CASE_INSENSITIVE);

	public ExecuteForEachEntryOfProcessor(Class<? extends Source> type, String typeValue) {
		super(type, typeValue);
	}

	@Override
	public Matcher getMatcher(String key) {
		return EXECUTE_FOR_EACH_ENTRY_OF_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		getSource(key, connector).setExecuteForEachEntryOf(
				value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1"));
	}
}