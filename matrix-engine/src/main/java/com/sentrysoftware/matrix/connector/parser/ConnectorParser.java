package com.sentrysoftware.matrix.connector.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.deserializer.ConnectorDeserializer;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.update.AvailableSourceUpdate;
import com.sentrysoftware.matrix.connector.update.CompiledFilenameUpdate;
import com.sentrysoftware.matrix.connector.update.ConnectorUpdateChain;
import com.sentrysoftware.matrix.connector.update.MonitorTaskSourceTreeUpdate;
import com.sentrysoftware.matrix.connector.update.PreSourceTreeUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class ConnectorParser {

	private ConnectorDeserializer deserializer;
	private NodeProcessor processor;
	private ConnectorUpdateChain connectorUpdateChain;

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
		final Connector connector = deserializer.deserialize(preNode);

		// Run the update chain
		if (connectorUpdateChain != null) {
			connectorUpdateChain.update(connector);
		}

		// Update the compiled filename
		new CompiledFilenameUpdate(file.getName()).update(connector);

		return connector;
	}

	/**
	 * Creates a new {@link ConnectorParser} with extends and constants
	 * {@link NodeProcessor}
	 * 
	 * @param connectorDirectory
	 * @return new instance of {@link ConnectorParser}
	 */
	private static ConnectorParser withNodeProcessor(final Path connectorDirectory) {
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		return ConnectorParser.builder()
			.deserializer(new ConnectorDeserializer(mapper))
			.processor(NodeProcessorHelper.withExtendsAndConstantsProcessor(connectorDirectory, mapper))
			.build();
	}

	/**
	 * Creates a new {@link ConnectorParser} with extends and constants
	 * {@link NodeProcessor} and with a {@link ConnectorUpdateChain}
	 * 
	 * @param connectorDirectory
	 * @return new instance of {@link ConnectorParser}
	 */
	public static ConnectorParser withNodeProcessorAndUpdateChain(final Path connectorDirectory) {
		final ConnectorParser connectorParser = withNodeProcessor(connectorDirectory);

		// Create the update objects
		final ConnectorUpdateChain availableSource = new AvailableSourceUpdate();
		final ConnectorUpdateChain preSourceTreeUpdate = new PreSourceTreeUpdate();
		final ConnectorUpdateChain monitorTaskSourceTree = new MonitorTaskSourceTreeUpdate();

		// Create the chain
		availableSource.setNextUpdateChain(preSourceTreeUpdate);
		preSourceTreeUpdate.setNextUpdateChain(monitorTaskSourceTree);

		// Set the first update chain
		connectorParser.setConnectorUpdateChain(availableSource);

		return connectorParser;
	}
}
