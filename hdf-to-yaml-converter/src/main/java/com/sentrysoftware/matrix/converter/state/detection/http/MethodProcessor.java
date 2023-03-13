package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.regex.Pattern;

public class MethodProcessor extends HttpProcessor{

	private static final Pattern METHOD_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.method\\s*$",
		Pattern.CASE_INSENSITIVE);
}
