package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * An abstract deserializer for collections of a specific type. Subclasses should provide implementations for
 * value extraction, empty collection creation, collector creation, and building a collection from another collection.
 * This deserializer handles the deserialization of JSON arrays and string values into a collection of the specified type.
 *
 * @param <T> The type of elements in the collection.
 * @see JsonDeserializer
 */
public abstract class AbstractCollectionDeserializer<T> extends JsonDeserializer<Collection<T>> {

	@Override
	public Collection<T> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return emptyCollection();
		}

		try {
			if (parser.isExpectedStartArrayToken()) {
				final Collection<String> strCollection = parser.readValueAs(new TypeReference<Collection<String>>() {});

				return Optional
					.ofNullable(strCollection)
					.map(collection -> collection.stream().map(valueExtractor()).filter(Objects::nonNull).collect(collector()))
					.orElse(emptyCollection());
			}

			return Optional
				.ofNullable(parser.getValueAsString())
				.map(str -> fromCollection(Collections.singleton(valueExtractor().apply(str))))
				.orElse(emptyCollection());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Value extractor function to execute when reading the string value
	 *
	 * @return {@link Function} which consumes a string value and returns T
	 */
	protected abstract Function<String, T> valueExtractor();

	/**
	 * Builds an empty collection
	 *
	 * @return {@link Collection} of T
	 */
	protected abstract Collection<T> emptyCollection();

	/**
	 * A mutable reduction function that accumulates values into a mutable
	 * result container
	 *
	 * @return {@link Collector} function
	 */
	protected abstract Collector<T, ?, Collection<T>> collector();

	/**
	 * Builds a collection from another collection
	 *
	 * @param collection
	 * @return new {@link Collection}
	 */
	protected abstract Collection<T> fromCollection(Collection<T> collection);
}
