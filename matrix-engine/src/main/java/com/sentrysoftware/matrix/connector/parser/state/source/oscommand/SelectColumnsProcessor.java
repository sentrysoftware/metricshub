package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.helper.SelectColumnsHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;

import lombok.NonNull;

public class SelectColumnsProcessor extends OsCommandProcessor {

	private static final Pattern SELECT_COLUMNS_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.SelectColumns\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return SELECT_COLUMNS_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {			
			((OSCommandSource) getSource(key, connector)).setSelectColumns(
					SelectColumnsHelper.convertToList(value));
		} catch (Exception e) {
			throw new IllegalStateException(
					String.format("SelectColumnsProcessor parse %s, error: %s", value, e.getMessage()));
		}
	}
}
