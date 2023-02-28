package com.sentrysoftware.matrix.connector.parser;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

public interface NodeProcessor {

	/**
	 * Process the given node then return the resulting {@link JsonNode}
	 * 
	 * @param node
	 * @return {@link JsonNode} instance
	 */
	JsonNode process(JsonNode node) throws IOException;
}
