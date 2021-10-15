package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class RemoveHeaderProcessor extends AbstractStateParser {

	private static final Pattern REMOVE_HEADER_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.removeHeader\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Source> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return REMOVE_HEADER_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		final Source source = getSource(key, connector);

		try {
			source.getClass()
			.getMethod("setRemoveHeader", Integer.class)
			.invoke(source, Integer.valueOf(value));

		} catch (final Exception e) {
			throw new IllegalStateException(
					String.format("RemoveHeaderProcessor parse %s, error: ", value, e.getMessage()), 
					e);
		}
	}
}
