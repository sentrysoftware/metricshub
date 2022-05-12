package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;

import lombok.NonNull;

public class TimeoutProcessor extends OsCommandProcessor {

	private static final Pattern TIMEOUT_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.timeout\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return TIMEOUT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((OsCommandSource) getSource(key, connector)).setTimeout(Long.valueOf(value));
		} catch (final Exception e) {
			throw new IllegalStateException(
					String.format("TimeoutProcessor parse %s, error: %s", value, e.getMessage()), 
					e);
		}
	}

}
