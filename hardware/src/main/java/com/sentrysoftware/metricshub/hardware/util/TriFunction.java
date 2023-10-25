package com.sentrysoftware.metricshub.hardware.util;

/**
 * This functional interface allows to apply a function to 3 parameters
 *  {@link java.util.function.BiFunction} applies a function to 2 parameters whereas TriFunction applies a function to 3 parameters
 */

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
	/**
	 * Applies this function to the given arguments.
	 * @param t first parameter
	 * @param u second parameter
	 * @param v third parameter
	 * @return of type R
	 */
	R apply(T t, U u, V v);
}
