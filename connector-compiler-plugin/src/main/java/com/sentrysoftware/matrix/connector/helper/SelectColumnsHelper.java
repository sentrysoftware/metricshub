package com.sentrysoftware.matrix.connector.helper;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SelectColumnsHelper {

	private static final Pattern VALUE_PATTERN = Pattern.compile("(\\d+)|(-\\d+)|(\\d+-\\d+)|(\\d+-)");
	
	// 1, 2, -4 or 1-3, 5, -6
	private static final Pattern INVALID_BEGIN_PATTERN = Pattern.compile("^(\\s*(\\d+|\\d+-\\d+)\\s*,)+\\s*-\\d+(\\s*,\\s*(\\d+|\\d+-\\d+)\\s*)*\\s*$");

	// 4-, 5 or 4-, 5-6
	private static final Pattern INVALID_END_PATTERN = Pattern.compile("^\\s*(\\s*(\\d+|\\d+-\\d+)\\s*,\\s*)*\\d+-\\s*(,\\s*(\\d+|\\d+-\\d+)\\s*)+$");

	/**
	 * Convert a selectColumns string into a list.
	 * 
	 * @param value The selectColumns string (mandatory)
	 * @return
	 */
	public static List<String> convertToList(
			@NonNull
			final String value) {
		if (value.isBlank()) {
			return Collections.emptyList();
		}

		if (INVALID_BEGIN_PATTERN.matcher(value).matches() || INVALID_END_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException(String.format("The SelectColumns %s is invalid.", value));
		}
		
		return Stream.of(value.split(","))
				.map(SelectColumnsHelper::checkValue)
				.collect(Collectors.toList());
	}

	/**
	 * <p>Check if the SelectColumn has the right value. Possible values:
	 * <li>a number, example: 1</li>
	 * <li>a couple of number, example: 1-3</li>
	 * <li>a dash with a number, example: -3</li>
	 * <li>a number with a dash, example: 3-</li>
	 * </p>
	 * @param value
	 * @return
	 */
	static String checkValue(final String value) {
		final String selectColumnValue = value.trim();
		if (VALUE_PATTERN.matcher(selectColumnValue).matches()) {
			return selectColumnValue;
		}
		throw new IllegalArgumentException(String.format("The SelectColumns value %s is invalid.", value));
	}
}
