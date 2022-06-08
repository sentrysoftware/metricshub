package com.sentrysoftware.hardware.agent.mapping;

import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingHelper {

	private static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("([a-z])([A-Z]+)");
	private static final Pattern SNAKE_CASE_PATTERN = Pattern.compile("(_)([a-z])");

	/**
	 * Converts a {@link String} written in camelCase to its snake_case version.<br>
	 * <b>Example: "parentId" -> "parent_id"</b>
	 *
	 * @param camelCase	The {@link String} that should be converted.
	 *
	 * @return			The snake_case version of the given {@link String}.
	 */
	public static String camelCaseToSnakeCase(String camelCase) {

		return CAMEL_CASE_PATTERN
				.matcher(camelCase)
				.replaceAll("$1_$2")
				.toLowerCase();
	}

	/**
	 * Converts a {@link String} written in snake_case to its camelCase version.<br>
	 * <b>Example: "parent_id" -> "parentId"</b>
	 *
	 * @param snakeCase	The {@link String} that should be converted.
	 *
	 * @return			The camelCase version of the given {@link String}.
	 */
	public static String snakeCaseToCamelCase(String snakeCase) {

		return SNAKE_CASE_PATTERN
			.matcher(snakeCase)
			.replaceAll(matchResult -> matchResult.group(2).toUpperCase());
	}

	/**
	 * Capitalize the given string
	 * 
	 * @param value
	 * @return String value
	 */
	public static String capitalize(String value) {
		return value.substring(0, 1).toUpperCase() + value.substring(1);
	}
}
