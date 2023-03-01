package com.sentrysoftware.matrix.connector.deserializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.Connector;

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
		final Connector connector = deserialize(new FileInputStream(src));

		updateCompiledFilename(connector, src.getName());

		return connector;
	}

	/**
	 * Deserialize the given YAML connector node
	 * 
	 * @param node     YAML as {@link TreeNode}
	 * @param fileName connector file name
	 * @return {@link Connector} instance
	 * @throws IOException
	 */
	public Connector deserialize(final TreeNode node, final String filename) throws IOException {
		final Connector connector =  JsonHelper.deserialize(mapper, node, Connector.class);

		updateCompiledFilename(connector, filename);

		return connector;
	}

	/**
	 * Update the Connector's compiled file name. The filename extension is removed
	 * from the original filename
	 * 
	 * @param connector {@link Connector} instance
	 * @param filename  The name of the connector file
	 */
	private void updateCompiledFilename(final Connector connector, final String filename) {
		connector.getOrCreateConnectorIdentity() // ConnectorIdentity might not be defined for the extended connectors (headers)
				.setCompiledFilename(filename.substring(0, filename.lastIndexOf('.')));
	}

}
