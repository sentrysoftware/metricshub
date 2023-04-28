package com.sentrysoftware.matrix.common.helpers;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;

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
	 * Deserialize the given input stream
	 * 
	 * @param <T>
	 * @param mapper {@link ObjectMapper} instance used to deserialize input stream
	 * @param input  {@link InputStream} instance
	 * @param type   Java type
	 * @return new instance of T
	 * @throws IOException
	 */
	public static <T> T deserialize(final ObjectMapper mapper, final InputStream input, final Class<T> type) throws IOException {

		return mapper.readValue(input, type);

	}

	/**
	 * Deserialize the given input stream
	 * 
	 * @param <T>
	 * @param mapper {@link ObjectMapper} instance used to deserialize input stream
	 * @param node   {@link TreeNode} instance
	 * @param type   Java type
	 * @return new instance of T
	 * @throws IOException
	 */
	public static <T> T deserialize(final ObjectMapper mapper, final TreeNode node, final Class<T> type) throws IOException {

		return mapper.treeToValue(node, type);

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
	 * Build and return a new {@link ObjectMapper} instance enabling the indentation.
	 * 
	 * @return new {@link ObjectMapper} instance
	 */
	public static ObjectMapper buildObjectMapper() {

		// Since 2.13 use JsonMapper.builder().enable(...)
		return JsonMapper.builder()
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
			.build();
	}

	/**
	 * build a new {@link ObjectMapper} using {@link YAMLFactory}
	 * 
	 * @return new {@link ObjectMapper} instance 
	 */
	public static ObjectMapper buildYamlMapper() {
		return JsonMapper
			.builder(new YAMLFactory().disable(Feature.SPLIT_LINES))
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_VALUES)
			.build();
	}
}