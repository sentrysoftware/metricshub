package com.sentrysoftware.matrix.common.helpers;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonHelper {

	private JsonHelper() { }

	/**
	 * Serialize the Java value to a JSON String
	 *  
	 * @param value the value object to serialize.
	 * @return a string containing the object serialized in JSON format.
	 */
	public static <T> String serialize(final T value) {

		try {
			return buildObjectMapper().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return "{}";
		}
	}

	/**
	 * Deserialize the JSON string to a Java value
	 * 
	 * @param json  The JSON value we wish to deserialize
	 * @param clazz The target Java type
	 * @return new Object
	 * @throws IOException
	 */
	public static <T> T deserialize(final String json, final Class<T> clazz) throws IOException {

		return buildObjectMapper().readValue(json, clazz);
	}

	/**
	 * Deserialize and return the requested type using the InputStream
	 * 
	 * @param is    {@link InputStream} connection to the JSON
	 * @param clazz The target Java type
	 * @return new Object
	 * @throws IOException
	 */
	public static <T> T deserialize(final InputStream is, final Class<T> clazz) throws IOException {

		return buildObjectMapper().readValue(is, clazz);
	}

	/**
	 * Build and return a new {@link ObjectMapper} instance enabling the indentation.<br>
	 * When no acessors are found for a type an empty Object is returned.
	 * 
	 * @return new {@link ObjectMapper} instance
	 */
	public static ObjectMapper buildObjectMapper() {
		return new ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
				.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
				.enable(SerializationFeature.INDENT_OUTPUT);
	}
}