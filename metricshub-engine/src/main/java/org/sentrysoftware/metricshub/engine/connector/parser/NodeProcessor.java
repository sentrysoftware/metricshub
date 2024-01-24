package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

/**
 * Interface for processing JsonNodes.
 */
public interface NodeProcessor {
	/**
	 * Processes the given JsonNode and returns the resulting {@link JsonNode}.
	 *
	 * @param node The JsonNode to process.
	 * @return {@link JsonNode} instance after processing.
	 * @throws IOException If an I/O error occurs during processing.
	 */
	JsonNode process(JsonNode node) throws IOException;
}
