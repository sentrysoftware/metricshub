package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TranslationTableProcessor extends ArrayTranslateProcessor {

	private static final Pattern TRANSLATION_TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.translationtable\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return TRANSLATION_TABLE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((ArrayTranslate) getCompute(key, connector)).setTranslationTable(getTranslationTable(value, connector));
	}
}
