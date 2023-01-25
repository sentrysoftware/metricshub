package com.sentrysoftware.matrix.connector.deserializer;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.Connector;

/**
 * This class deserializes YAML connector files 
 *
 */
public class ConnectorDeserializer {

	private ObjectMapper mapper;

	public ConnectorDeserializer() {
		mapper = JsonMapper
			.builder(new YAMLFactory())
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
			.build();
	}

	/**
	 * Deserialize the given YAML connector file
	 * 
	 * @param input
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final InputStream input) throws IOException {
		return JsonHelper.deserialize(mapper, input, Connector.class);
	}
}
