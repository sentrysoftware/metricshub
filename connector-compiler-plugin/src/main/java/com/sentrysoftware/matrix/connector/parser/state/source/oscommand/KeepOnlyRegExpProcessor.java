package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;

import lombok.NonNull;

public class KeepOnlyRegExpProcessor extends OsCommandProcessor {

	private static final Pattern KEEP_ONLY_REGEXP_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.KeepOnlyRegExp\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return KEEP_ONLY_REGEXP_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((OSCommandSource) getSource(key, connector)).setKeepOnlyRegExp(value);
	}
}
