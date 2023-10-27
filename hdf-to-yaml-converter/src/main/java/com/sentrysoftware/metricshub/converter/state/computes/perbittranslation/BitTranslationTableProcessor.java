package com.sentrysoftware.metricshub.converter.state.computes.perbittranslation;

import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import com.sentrysoftware.metricshub.converter.state.computes.common.TranslationTableProcessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BitTranslationTableProcessor extends TranslationTableProcessor {

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildComputeKeyRegex("bittranslationtable"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}
}