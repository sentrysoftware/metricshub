package com.sentrysoftware.matrix.common.helpers;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

public class StreamUtils {

	private StreamUtils() {}

	/**
	 * Reverse the given {@link Stream} using a {@link Deque}
	 * @param <T>
	 * @param stream
	 * @return reversed {@link Stream}
	 */
	public static <T> Stream<T> reverse(final Stream<T> stream) {
		final Deque<T> deque = new ArrayDeque<>();
		stream.forEach(deque::push);
		return deque.stream();
	}

}
