package com.sentrysoftware.matrix.common.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberHelper {

	public static final Pattern INTEGER_DETECT_PATTERN = Pattern.compile("^(-?\\d+)(\\.0*)$");

	private NumberHelper() {}

	/**
	 * Parse the given double value, if the parsing fails return the default value
	 * 
	 * @param value        The value we wish to parse
	 * @param defaultValue The default value to return if the parsing fails
	 * @return {@link Double} value
	 */
	public static Double parseDouble(String value, Double defaultValue) {

		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Parse the given integer value, if the parsing fails return the default value
	 * 
	 * @param value        The value we wish to parse
	 * @param defaultValue The default value to return if the parsing fails
	 * @return {@link Integer} value
	 */
	public static Integer parseInt(String value, Integer defaultValue) {

		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Round the given double value using the specified decimal places
	 * 
	 * @param value        The value we wish to round
	 * @param places       The required decimal places expected as positive
	 * @param roundingMode The rounding behavior used by the {@link BigDecimal} object for the numerical operations
	 * @return double value
	 */
	public static double round(final double value, final int places, final RoundingMode roundingMode) {
		if (places < 0)
			throw new IllegalArgumentException();

		 return BigDecimal.valueOf(value)
				 .setScale(places, roundingMode)
				 .doubleValue();
	}

	/**
	 * Removes the fractional part and the decimal point of the given state if the
	 * fractional part contains only 0 after the decimal point
	 * 
	 * @param state the value we wish to process
	 * @return String value
	 */
	public static String formatIntegerState(final String state) {

		if (state == null) {
			return null;
		}

		final Matcher matcher = INTEGER_DETECT_PATTERN.matcher(state);

		if (matcher.find()) {
			return state.substring(0, state.indexOf(matcher.group(2)));
		}

		return state;
	}
}
