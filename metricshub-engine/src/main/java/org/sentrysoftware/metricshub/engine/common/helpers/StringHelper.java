package org.sentrysoftware.metricshub.engine.common.helpers;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.COMMA;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Helper class for working with strings.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringHelper {

	/**
	 * Execute the given callable to get the resulting Object as String value.
	 *
	 * @param call         Callable providing a value.
	 * @param defaultValue The default value to return if the callable returns null or empty.
	 * @return String value.
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

	/**
	 * Iterates over all the throwable causes and extract all the messages.
	 *
	 * @param throwable The {@link Throwable} instance we wish to process
	 * @return String value
	 */
	public static String getStackMessages(final Throwable throwable) {
		if (throwable == null) {
			return EMPTY;
		}

		return new StringBuilder(throwable.getClass().getSimpleName())
			.append(": ")
			.append(throwable.getMessage())
			.append("\n")
			.append(
				Stream
					.iterate(throwable, Objects::nonNull, Throwable::getCause)
					.filter(th -> th != throwable)
					.map(th -> String.format("Caused by %s: %s", th.getClass().getSimpleName(), th.getMessage()))
					.collect(Collectors.joining("\n"))
			)
			.toString();
	}

	/**
	 * Format the given HTTP headers map as the following example:<br>
	 * <code>
	 * Content-Type: application/json<br>
	 * Connection: keep-alive
	 * </code>
	 *
	 * @param headers Key-Value collection
	 * @return String value
	 */
	public static String prettyHttpHeaders(final Map<String, String> headers) {
		if (headers == null) {
			return EMPTY;
		}

		return headers
			.entrySet()
			.stream()
			.map(entry -> String.format("%s: %s", entry.getKey(), entry.getValue()))
			.sorted()
			.collect(Collectors.joining("\n"));
	}

	/**
	 * Add the given prefix and value to the {@link StringJoiner} instance. <code>null</code> value is not added.
	 *
	 * @param <T>            the type of the value to be added
	 * @param stringJoiner   {@link StringJoiner} instance used to append the prefix and the value.
	 * @param prefix         The value prefix.
	 * @param value          The value to add.
	 */
	public static <T> void addNonNull(
		@NonNull final StringJoiner stringJoiner,
		@NonNull final String prefix,
		final T value
	) {
		if (value != null) {
			stringJoiner.add(new StringBuilder(prefix).append(value));
		}
	}

	/**
	 * Replace each substring of the <code>template</code> that matches the literal
	 * <code>macro</code> sequence with the value specified by the <code>replacementSupplier</code>.
	 *
	 * @param macro               The sequence of char values to be replaced
	 * @param replacementSupplier The supplier of the replacement sequence
	 * @param template            The template to replace
	 * @return String value
	 */
	public static String replace(
		@NonNull final String macro,
		@NonNull final Supplier<String> replacementSupplier,
		@NonNull final String template
	) {
		if (template.contains(macro)) {
			return template.replace(macro, getValue(replacementSupplier::get, macro));
		}

		return template;
	}

	/**
	 * Replace each substring of the <code>template</code> that matches the
	 * literal <code>macro</code> sequence with the specified literal
	 * <code>replacement</code> sequence.
	 *
	 * @param macro       The sequence of char values to be replaced
	 * @param replacement The replacement sequence
	 * @param template    The template to replace
	 * @return String value
	 */
	public static String replace(@NonNull final String macro, final String replacement, @NonNull final String template) {
		if (template.contains(macro) && replacement != null) {
			return template.replace(macro, replacement);
		}

		return template;
	}

	/**
	 * Convert the given value to a string representation. If the value is a collection or an array,
	 * it is transformed into a CSV (Comma-Separated Values) string.
	 *
	 * @param value The value to be converted to a string.
	 * @return The string representation of the value, or a CSV string if the value is a collection or an array.
	 */
	public static String stringify(final Object value) {
		if (value == null) {
			// Handle null input
			return EMPTY;
		} else if (value instanceof Collection<?> collection) {
			// If the input is a List, convert it to a CSV string
			return collection.stream().map(item -> item != null ? item.toString() : EMPTY).collect(Collectors.joining(COMMA));
		} else if (value.getClass().isArray()) {
			// If the input is an array, convert it to a CSV string
			Object[] array = (Object[]) value;
			return Arrays
				.stream(array)
				.map(item -> item != null ? item.toString() : EMPTY)
				.collect(Collectors.joining(COMMA));
		} else {
			// For any other type of value, simply convert it to a string
			return value.toString();
		}
	}
}
