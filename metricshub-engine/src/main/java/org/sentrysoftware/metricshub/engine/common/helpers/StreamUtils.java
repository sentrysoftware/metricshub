package org.sentrysoftware.metricshub.engine.common.helpers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

/**
 * Utility class for working with streams.
 */
public class StreamUtils {

	private StreamUtils() {}

	/**
	 * Reverse the given {@link Stream} using a {@link Deque}.
	 *
	 * @param <T>    The type of elements in the stream.
	 * @param stream The input stream.
	 * @return Reversed {@link Stream}.
	 */
	public static <T> Stream<T> reverse(final Stream<T> stream) {
		final Deque<T> deque = new ArrayDeque<>();
		stream.forEach(deque::push);
		return deque.stream();
	}
}
