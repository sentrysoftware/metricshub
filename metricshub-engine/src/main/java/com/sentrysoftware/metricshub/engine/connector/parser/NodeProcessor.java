package com.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;

public interface NodeProcessor {
	/**
	 * Process the given node then return the resulting {@link JsonNode}
	 *
	 * @param node
	 * @return {@link JsonNode} instance
	 */
	JsonNode process(JsonNode node) throws IOException;
}
