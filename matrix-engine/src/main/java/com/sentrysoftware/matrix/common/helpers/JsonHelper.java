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
			return getNewObjectMapper().writeValueAsString(value);
		} catch (JsonProcessingException e) {
			return "{}";
		}
	}

	public static ObjectMapper getNewObjectMapper() {
		return new ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
				.enable(SerializationFeature.INDENT_OUTPUT);
	}
}