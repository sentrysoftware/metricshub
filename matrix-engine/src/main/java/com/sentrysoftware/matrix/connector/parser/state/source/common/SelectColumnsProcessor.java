package com.sentrysoftware.matrix.connector.parser.state.source.common;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.helper.SelectColumnsHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class SelectColumnsProcessor extends AbstractStateParser {

	private static final Pattern SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.SelectColumns\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Source> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		final Source source = getSource(key, connector);

		try {			
			source.getClass()
			.getMethod("setSelectColumns", List.class)
			.invoke(source, SelectColumnsHelper.convertToList(value));

		} catch (Exception e) {
			throw new IllegalStateException(
					String.format("SelectColumnsProcessor parse %s, error: %s", value, e.getMessage()));
		}
	}
}
