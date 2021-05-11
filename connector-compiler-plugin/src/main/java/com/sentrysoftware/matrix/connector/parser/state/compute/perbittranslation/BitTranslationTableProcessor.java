package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

public class BitTranslationTableProcessor extends PerBitTranslationProcessor {

	private static final Pattern TRANSLATION_TABLE_KEY_PATTERN = Pattern.compile(
		"^\\s*(.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)\\.compute\\(([1-9]\\d*)\\)\\.bittranslationtable\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return TRANSLATION_TABLE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key + ConnectorParserConstants.DOT);

		PerBitTranslation perBitTranslation = getCompute(getSource(matcher, connector), getComputeIndex(matcher));
		notNull(perBitTranslation,
			() -> "Could not find any Compute for the following key: " + key + ConnectorParserConstants.DOT);

		Map<String, TranslationTable> translationTables = connector.getTranslationTables();
		state(translationTables != null, () -> "No translation tables found in " + connector.getCompiledFilename());

		TranslationTable translationTable = translationTables.get(value);
		state(translationTable != null, () -> "Could not find translation table " + value + " in " + connector.getCompiledFilename());

		perBitTranslation.setBitTranslationTable(translationTable);
	}
}
