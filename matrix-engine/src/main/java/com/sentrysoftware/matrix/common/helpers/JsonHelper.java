package com.sentrysoftware.matrix.common.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
	 * Build and return a new {@link ObjectMapper} instance enabling the indentation.<br>
	 * When no acessors are found for a type an empty Object is returned.
	 * 
	 * @return new {@link ObjectMapper} instance
	 */
	public static ObjectMapper buildObjectMapper() {
		return new ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.enable(SerializationFeature.INDENT_OUTPUT);
	}
}