package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;

import lombok.NonNull;

public class RemoveHeaderProcessor extends OsCommandProcessor {

	private static final Pattern REMOVE_HEADER_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.removeHeader\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return REMOVE_HEADER_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((OSCommandSource) getSource(key, connector)).setRemoveHeader(Integer.valueOf(value));
		} catch (final Exception e) {
			throw new IllegalStateException(
					String.format("RemoveHeaderProcessor parse %s, error: ", value, e.getMessage()), 
					e);
		}
	}
}
