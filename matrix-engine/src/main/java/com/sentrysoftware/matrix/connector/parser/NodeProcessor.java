package com.sentrysoftware.matrix.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;

public interface NodeProcessor {

	/**
	 * Process the given node then return the resulting {@link JsonNode}
	 * 
	 * @param node
	 * @return {@link JsonNode} instance
	 */
	JsonNode process(JsonNode node);
}
