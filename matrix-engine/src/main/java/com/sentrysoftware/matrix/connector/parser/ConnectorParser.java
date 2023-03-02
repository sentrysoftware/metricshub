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
import com.sentrysoftware.matrix.connector.update.MonitorTaskSourceDepUpdate;
import com.sentrysoftware.matrix.connector.update.PreSourceDepUpdate;

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

		JsonNode node = deserializer.getMapper().readTree(file);

		// PRE-Processing
		if (processor != null) {
			node = processor.process(node);
		}

		// POST-Processing
		final Connector connector = deserializer.deserialize(node);

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
	public static ConnectorParser withNodeProcessor(final Path connectorDirectory) {
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
		final ConnectorUpdateChain preSourceDepUpdate = new PreSourceDepUpdate();
		final ConnectorUpdateChain monitorTaskSourceDepUpdate = new MonitorTaskSourceDepUpdate();

		// Create the chain
		availableSource.setNextUpdateChain(preSourceDepUpdate);
		preSourceDepUpdate.setNextUpdateChain(monitorTaskSourceDepUpdate);

		// Set the first update chain
		connectorParser.setConnectorUpdateChain(availableSource);

		return connectorParser;
	}
}
