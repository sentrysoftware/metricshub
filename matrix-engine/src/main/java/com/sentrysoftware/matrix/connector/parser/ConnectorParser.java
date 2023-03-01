package com.sentrysoftware.matrix.connector.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.deserializer.ConnectorDeserializer;
import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class ConnectorParser {

	private ConnectorDeserializer deserializer;
	private NodeProcessor processor;

	/**
	 * Parse the given connector file
	 * 
	 * @param file
	 * @return new {@link Connector} object
	 * @throws IOException
	 */
	public Connector parse(final File file) throws IOException {

		final JsonNode node = deserializer.getMapper().readTree(file);

		// PRE-Processing
		final JsonNode preNode = processor.process(node);

		// POST-Processing
		return deserializer.deserialize(
			preNode,
			file.getName()
		);
	}

	/**
	 * Creates a new {@link ConnectorParser} with extends and constants
	 * {@link NodeProcessor}
	 * 
	 * @param connectorDirectory
	 * @return new instance of {@link ConnectorParser}
	 */
	public static ConnectorParser withNodeProcessor(final Path connectorDirectory) {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		return ConnectorParser.builder()
			.deserializer(new ConnectorDeserializer(mapper))
			.processor(NodeProcessorHelper.withExtendsAndConstantsProcessor(connectorDirectory, mapper))
			.build();
	}
}
