package org.sentrysoftware.metricshub.engine.common.helpers;

import java.util.List;

/**
 * Helper class for common operations on lists.
 */
public class ListHelper {

	private ListHelper() {}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * @param <T>           The type of elements in the list.
	 * @param list          The list from which we want to get the element at the specified position.
	 * @param index         Index of the element to return.
	 * @param defaultValue  The default value to return if the element cannot be extracted.
	 * @return The element at the specified position in the list, or the default value if the index is out of bounds.
	 */
	public static <T> T getValueAtIndex(List<T> list, int index, T defaultValue) {
		if (list == null || list.size() <= index) {
			return defaultValue;
		}
		return list.get(index);
	}
}
