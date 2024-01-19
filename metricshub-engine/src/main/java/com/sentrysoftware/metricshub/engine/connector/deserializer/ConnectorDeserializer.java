package com.sentrysoftware.metricshub.engine.connector.deserializer;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.metricshub.engine.common.helpers.JsonHelper;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;

/**
 * This class deserializes YAML connector files
 *
 */
public class ConnectorDeserializer {

	@Getter
	private ObjectMapper mapper;

	public ConnectorDeserializer(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	/**
	 * Deserialize the given YAML connector input stream
	 *
	 * @param input YAML as {@link InputStream}
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final InputStream input) throws IOException {
		return JsonHelper.deserialize(mapper, input, Connector.class);
	}

	/**
	 * Deserialize the given YAML connector file
	 *
	 * @param src YAML file
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final File src) throws IOException {
		return deserialize(new FileInputStream(src));
	}

	/**
	 * Deserialize the given YAML connector node
	 *
	 * @param node     YAML as {@link TreeNode}
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final TreeNode node) throws IOException {
		return JsonHelper.deserialize(mapper, node, Connector.class);
	}
}
