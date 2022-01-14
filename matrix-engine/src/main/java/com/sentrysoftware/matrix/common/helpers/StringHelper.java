package com.sentrysoftware.matrix.common.helpers;

import java.util.concurrent.Callable;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringHelper {

	/**
	 * Execute the given callable to get the resulting Object as String value
	 * 
	 * @param call         Callable providing a value
	 * @param defaultValue The default value to return if the callable returns null
	 *                     or empty
	 * @return String value
	 */
	public static String getValue(final Callable<Object> call, final String defaultValue) {
		final Object result = callIfPossible(call);
		final String value = (result != null) ? result.toString() : null;
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Call the callable and return the result. Return <code>null</code> if an exception occurs
	 * 
	 * @param call callback to run
	 * @return Object value
	 */
	private static Object callIfPossible(final Callable<Object> call) {
		try {
			return call.call();
		} catch (Exception ex) {
			return null;
		}
	}
}
