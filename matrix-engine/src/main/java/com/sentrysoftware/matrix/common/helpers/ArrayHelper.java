package com.sentrysoftware.matrix.common.helpers;

public class ArrayHelper {

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
}
