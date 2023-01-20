package com.sentrysoftware.matrix.connector.deserializer;

import java.io.File;
import java.io.IOException;

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
	 * @param src
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final File src) throws IOException {
		return JsonHelper.deserialize(mapper, src, Connector.class);
	}
}
