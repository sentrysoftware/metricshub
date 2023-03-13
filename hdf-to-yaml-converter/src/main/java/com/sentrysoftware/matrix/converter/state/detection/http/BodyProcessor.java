package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.converter.ConverterConstants;

public class BodyProcessor extends HttpProcessor{

	private static final Pattern BODY_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.body\\s*$",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		ConverterConstants.EMBEDDED_FILE_REGEX,
		Pattern.CASE_INSENSITIVE);

}
