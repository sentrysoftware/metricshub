package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

public class TranslationTableProcessor extends TranslateProcessor {

	private static final Pattern TRANSLATION_TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.translationtable\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return TRANSLATION_TABLE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), "Invalid key: " + key + ConnectorParserConstants.DOT);

		Source source = getSource(matcher, connector);

		Translate translate = getTranslate(source, getComputeIndex(matcher));
		notNull(translate,
				"Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		Map<String, TranslationTable> translationTables = connector.getTranslationTables();
		state(translationTables != null, "No translation tables found in " + connector.getCompiledFilename());

		String strippedValue = value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1");
		TranslationTable translationTable = translationTables.get(strippedValue);
		state(translationTable != null,
				"Could not find translation table " + strippedValue + " in " + connector.getCompiledFilename());

		translate.setTranslationTable(translationTable);
	}
}
