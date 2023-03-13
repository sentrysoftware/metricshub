package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Pattern;

public class UrlProcessor extends HttpProcessor{

	private static final Pattern URL_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.url\\s*$",
		Pattern.CASE_INSENSITIVE);
}
