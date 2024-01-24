package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.Objects;
import java.util.function.Function;

/**
 * An abstract base class for deserializers handling non-null and non-blank values in collections.
 * Extends {@link AbstractCollectionDeserializer} and provides an implementation for extracting
 * non-null and non-blank values using a {@link Function}.
 */
public abstract class AbstractNonBlankNonNullInCollectionDeserializer extends AbstractCollectionDeserializer<String> {

	@Override
	protected Function<String, String> valueExtractor() {
		return nonNullNonBlankExtractor();
	}

	/**
	 * Return a function that extracts a non-null and non-blank value
	 *
	 * @return {@link Function} instance
	 */
	private Function<String, String> nonNullNonBlankExtractor() {
		return str -> {
			if (Objects.nonNull(str) && !str.isBlank()) {
				return str;
			}

			throw new IllegalArgumentException(getErrorMessage());
		};
	}

	protected abstract String getErrorMessage();
}
