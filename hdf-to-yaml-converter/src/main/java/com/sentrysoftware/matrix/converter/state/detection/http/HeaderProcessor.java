package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Pattern;

import com.sentrysoftware.matrix.converter.ConverterConstants;

public class HeaderProcessor extends HttpProcessor {

	private static final Pattern HEADER_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.header\\s*$",
		Pattern.CASE_INSENSITIVE);

	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		ConverterConstants.EMBEDDED_FILE_REGEX,
		Pattern.CASE_INSENSITIVE);
}
