package com.sentrysoftware.matrix.connector.parser;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.ConnectorState;
import com.sentrysoftware.matrix.utils.Assert;

import lombok.Data;

@Data
public class ConnectorParser {

	private String connectorFilePath;

	public ConnectorParser(final String connectorFilePath) {

		Assert.isTrue(connectorFilePath != null && !connectorFilePath.trim().isEmpty(), "connectorFilePath cannot be null or empty");

		this.connectorFilePath = connectorFilePath;
	}

	/**
	 * Process Connector file 
	 * @return {@link Optional} of {@link Connector} instance
	 */
	public Optional<Connector> parse() {

		try {

			final ConnectorRefined connectorRefined = new ConnectorRefined();

			connectorRefined.load(connectorFilePath);

			return Optional.of(parseContent(connectorRefined));
		} catch (Exception e) {
			Assert.state(false, () -> String.format("Cannot load Connector file %s. Message: %s",
					connectorFilePath, e.getMessage()));
		}

		return Optional.empty();
	}

	/**
	 * From the given {@link ConnectorRefined} object parse the whole Connector content
	 * @param connectorRefined
	 * @return {@link Connector} instance.
	 */
	private static Connector parseContent(final ConnectorRefined connectorRefined) {

		final Connector connector = new Connector();

		connector.setEmbeddedFiles(connectorRefined.getEmbeddedFiles());
		connector.setTranslationTables(connectorRefined.getTranslationTables());
		connector.setCompiledFilename(connectorRefined.getCompiledFilename());

		connectorRefined.getCodeMap().forEach((key, value) -> parseKeyValue(key, value, connector));

		return connector;

	}

	/**
	 * Detect and parse the given line
	 * @param key the Connector key we wish to extract its value
	 * @param value the corresponding value we wish to process
	 * @param connector {@link Connector} instance to update
	 */
	private static void parseKeyValue(final String key, final String value, final Connector connector) {

		// Get the detected state
		final Set<ConnectorState> connectorStates = ConnectorState.getConnectorStates().stream().filter(state -> state.detect(key, value, connector))
				.collect(Collectors.toSet());

		 Optional<ConnectorState> stateOpt = connectorStates.stream().findFirst();
		 if (stateOpt.isPresent()) {
			 stateOpt.get().parse(key, value, connector);
		 }
		// TODO if the Connector defines an IPMI source then add "ipmitool" to the list of sudoCommands in the Connector bean.
	}

}
