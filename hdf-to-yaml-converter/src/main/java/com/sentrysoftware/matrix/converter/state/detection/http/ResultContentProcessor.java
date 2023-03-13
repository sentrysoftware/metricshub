package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Pattern;

public class ResultContentProcessor extends HttpProcessor{

	private static final Pattern RESULT_CONTENT_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.resultcontent\\s*$",
		Pattern.CASE_INSENSITIVE);
}
