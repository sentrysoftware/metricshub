package com.sentrysoftware.matrix.common.helpers;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArrayHelper {

	private ArrayHelper() {}

	/**
	 * Returns the element at the specified position in this array.
	 * @param <T>
	 *
	 * @param array        The array from which we want to get the element at the specified position
	 * @param index        Index of the element to return
	 * @param defaultValue The default value to return if the element cannot be extracted
	 * @return T
	 */
	public static <T> T getValueAtIndex(T[] array, int index, T defaultValue) {
		if (array == null || array.length <= index) {
			return defaultValue;
		}
		return array[index];
	}

	/**
	 * Check if the given data matches using the predicate function.<br>
	 * Each data element is converted to lower case before applying the predicate.
	 *
	 * @param predicate boolean-valued function to check the passed data
	 * @param data      the input to the predicate
	 * @return <code>true</code> if one of the data matched otherwise <code>false</code>
	 */
	public static boolean anyMatchLowerCase(final Predicate<String> predicate, final String... data) {
		return Arrays.stream(data).filter(Objects::nonNull).map(String::toLowerCase).anyMatch(predicate);
	}

	/**
	 * Regular expression that extracts in group(1) the hexadecimal data (trimming white spaces and 0x or # prefix)
	 */
	private static final Pattern HEX_PATTERN = Pattern.compile(
		"^\\s*(?:0x|#)?([0-9a-f]*)\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * Convert the specified hexadecimal string into a byte array.
	 * <p>
	 * Example:
	 * <p>
	 * "0x010203" => <code>new byte[] { 0x01, 0x02, 0x03 }</code>
	 * <p>
	 * @param hexString Hexadecimal string (may be prefixed with "0x" or "#")
	 * @return the corresponding byte array
	 */
	public static byte[] hexToByteArray(String hexString) {
		// Null => null
		if (hexString == null) {
			return new byte[] {};
		}

		// Extract the hexadecimal data (remove 0x or # prefix)
		Matcher hexMatcher = ArrayHelper.HEX_PATTERN.matcher(hexString);
		if (!hexMatcher.find()) {
			throw new IllegalArgumentException("Invalid hexadecimal data: " + hexString);
		}
		final String hexData = hexMatcher.group(1);

		final int len = hexData.length();
		if (len % 2 != 0) {
			throw new IllegalArgumentException("Missing or extraneous digit in " + hexData);
		}

		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(hexData.charAt(i), 16) << 4) + Character.digit(hexData.charAt(i + 1), 16));
		}

		return data;
	}
}
