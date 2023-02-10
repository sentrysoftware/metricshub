package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;
import java.util.Map;
import java.util.StringJoiner;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

public abstract class AbstractMapDeserializer<T> extends JsonDeserializer<Map<String, T>> {

	protected String nodePath;

	@Override
	public Map<String, T> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null)
			return emptyMap();

		nodePath = getNodePath(parser.getParsingContext().getParent(), new StringJoiner(".", "$", ""));

		final Map<String, T> map = parser.readValueAs(getTypeReference());

		if (map == null) {
			return emptyMap();
		}

		if (!isValidMap(map)) {
			throw new InvalidFormatException(
				parser,
				messageOnInvalidMap(parser.getCurrentName()),
				map,
				Map.class
			);
		}

		updateMapValues(parser, ctxt, map);

		if (isExpectedInstance(map)) {
			return map;
		}

		return fromMap(map);
	}

	/**
	 * Get the current node path E.g. monitors.enclosure.discovery.sources
	 * 
	 * @param context streaming processing contexts used during reading the content
	 * @param stringJoiner StringJoiner is used to construct a sequence of characters separated by a delimiter
	 * @return String value
	 */
	private String getNodePath(final JsonStreamContext context, final StringJoiner stringJoiner) {
		// Recursively call the parent of the context to build the full node path 
		// Stop if the parent is null, it means that root parent is reached
		if (context != null && context.getParent() != null) {
			if (context.inObject()) {
				stringJoiner.add(context.getCurrentName());
			} else if (context.inArray()) {
				stringJoiner.add(String.format("[%s]", context.getCurrentIndex()));
			}

			getNodePath(context.getParent(), stringJoiner);
		}

		return stringJoiner.toString();

	}

	/**
	 * Update the given map using the current parser and its context
	 * 
	 * @param parser
	 * @param ctxt Context that can be used to access information about this deserialization activity
	 * @param map  Parsed used for reading JSON content
	 */
	protected abstract void updateMapValues(JsonParser parser, DeserializationContext ctxt, Map<String, T> map);

	/**
	 * Get the error message to display when the map is invalid
	 * 
	 * @param nodeKey the current node key. E.g. sources
	 * @return String value
	 */
	protected abstract String messageOnInvalidMap(String nodeKey);

	/**
	 * Builds a map from the map passed as argument
	 * 
	 * @param map
	 * @return new {@link Map}
	 */
	protected abstract Map<String, T> fromMap(Map<String, T> map);

	/**
	 * Whether the map is expected or not
	 * 
	 * @param map
	 * @return <code>true</code> if the map is in an expected type otherwise
	 * false.
	 */
	protected abstract boolean isExpectedInstance(Map<String, T> map);

	/**
	 * Create a new empty map
	 * 
	 * @return new {@link Map}
	 */
	protected abstract Map<String, T> emptyMap();

	/**
	 * Whether the given map is valid or not
	 * 
	 * @param map
	 * @return <code>true</code> if the map is valid otherwise false
	 */
	protected abstract boolean isValidMap(Map<String, T> map);

	/**
	 * Create the Java type to read content as (passed to ObjectCodec that
	 * deserializes content)
	 * 
	 * @return a new {@link TypeReference}
	 */
	protected abstract TypeReference<Map<String, T>> getTypeReference();

}
