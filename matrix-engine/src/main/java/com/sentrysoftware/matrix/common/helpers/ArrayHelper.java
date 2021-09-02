package com.sentrysoftware.matrix.common.helpers;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

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
	 * Check if the given data matches using the predicate function
	 * 
	 * @param predicate boolean valued function to check the passed data
	 * @param data      the input to the predicate
	 * @return <code>true</code> if one of the data matched otherwise <code>false</code>
	 */
	public static boolean anyMatch(final Predicate<String> predicate, final String... data) {
		return Arrays.stream(data)
				.filter(Objects::nonNull)
				.map(String::toLowerCase)
				.anyMatch(predicate);
	}
}
