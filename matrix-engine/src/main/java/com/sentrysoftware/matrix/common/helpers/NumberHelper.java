package com.sentrysoftware.matrix.common.helpers;

public class NumberHelper {
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
}
