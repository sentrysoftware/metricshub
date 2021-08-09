package com.sentrysoftware.matrix.common.helpers;

public class NumberHelper {

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
	 * @param value   The value we wish to round
	 * @param places  The required decimal places expected as positive
	 * @return double value
	 */
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}
