package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.lang.reflect.InvocationTargetException;
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
public class SeparatorsProcessor extends AbstractStateParser {

	private static final Pattern SEPARATORS_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Separators\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Source> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return SEPARATORS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		final Source source = getSource(key, connector);
		
		try {
			source.getClass()
			.getMethod("setSeparators", String.class)
			.invoke(source, value);

		} catch (
				IllegalAccessException |
				IllegalArgumentException |
				InvocationTargetException |
				NoSuchMethodException |
				SecurityException e) {
			throw new IllegalStateException(
					String.format("SeparatorsProcessor parse: cannot invoke %s (%s) on Source: %s",
							"setSeparators",
							value, 
							e.getMessage()), 
					e);
		}
	}
}
