package com.sentrysoftware.matrix.common.helpers;

import java.util.List;

public class ListHelper {

	/**
	 * Returns the element at the specified position in this list.
	 * @param <T>
	 * 
	 * @param list         The list from which we want to get the element at the specified position.
	 * @param index        Index of the element to return
	 * @param defaultValue The default value to return if the element cannot be extracted.
	 * @return String value
	 */
	public static <T> T getValueAtIndex(List<T> list, int index, T defaultValue) {
		if (list == null || list.size() <= index) {
			return defaultValue;
		}
		return list.get(index);
	}
}
