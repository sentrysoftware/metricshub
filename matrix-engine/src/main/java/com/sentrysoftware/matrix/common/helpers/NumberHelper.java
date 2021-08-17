package com.sentrysoftware.matrix.common.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
}
