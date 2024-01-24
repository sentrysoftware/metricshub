package org.sentrysoftware.metricshub.engine.connector.parser;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Abstract base class for implementing a chain of responsibility pattern in processing JsonNodes.
 * Each concrete subclass represents a specific processing step in the chain.
 */
@AllArgsConstructor
@Data
public abstract class AbstractNodeProcessor {

	/**
	 * Next node processor
	 */
	protected AbstractNodeProcessor next;

	/**
	 * Process the provided {@link JsonNode} with the remaining chain of processors.
	 *
	 * @param node The JsonNode to be processed.
	 * @return An instance of {@link JsonNode} representing the result of the processing.
	 * @throws IOException If an I/O error occurs during the processing.
	 */
	public JsonNode process(final JsonNode node) throws IOException {
		final JsonNode processedNode = processNode(node);

		if (next != null) {
			return next.process(processedNode);
		}

		return processedNode;
	}

	/**
	 * Process one {@link JsonNode}.
	 *
	 * @param node The JsonNode to be processed.
	 * @return An instance of {@link JsonNode} representing the result of the processing.
	 * @throws IOException If an I/O error occurs during the processing.
	 */
	protected abstract JsonNode processNode(JsonNode node) throws IOException;
}
