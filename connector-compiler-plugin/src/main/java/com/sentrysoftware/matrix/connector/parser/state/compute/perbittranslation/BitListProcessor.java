package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

public class BitListProcessor extends PerBitTranslationProcessor {

	private static final Pattern BIT_LIST_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.bitlist\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return BIT_LIST_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		PerBitTranslation perBitTranslation = getCompute(key, connector);

		perBitTranslation.setBitList(Arrays
			.stream(value.split(COMMA))
			.map(Integer::parseInt)
			.collect(Collectors.toList()));
	}
}
