package com.sentrysoftware.matrix.converter.state.source.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;

public abstract class AbstractExecuteForEach extends AbstractStateConverter {

	protected static final String CONCAT_METHOD = "concatMethod";
	protected static final String EXECUTE_FOR_EACH_ENTRY_OF = "executeForEachEntryOf";

	/**
	 * Get or create the executeForEachEntryOf {@link ObjectNode}
	 *
	 * @param key The key used to extract the source
	 * @param connector The global connector object
	 * @return {@link ObjectNode} instance
	 */
	protected ObjectNode getOrCreateExecuteForEachEntryOf(final String key, final JsonNode connector) {
		final ObjectNode source = getCurrentSource(key, connector);

		JsonNode executeForEachEntryOf = source.get(EXECUTE_FOR_EACH_ENTRY_OF);

		if (executeForEachEntryOf == null) {
			executeForEachEntryOf = JsonNodeFactory.instance.objectNode();
			source.set(EXECUTE_FOR_EACH_ENTRY_OF, executeForEachEntryOf);
			return (ObjectNode) executeForEachEntryOf;
		}

		return (ObjectNode) executeForEachEntryOf;
	}

	/**
	 * Get or create the concatMethod as {@link ObjectNode} in the given connector
	 *
	 * @param key The key used to extract the source
	 * @param connector The global connector object
	 * @return {@link ObjectNode} instance
	 */
	protected ObjectNode getOrCreateCustomConcatMethod(final String key, final JsonNode connector) {
		final ObjectNode executeForEachEntryOf = getOrCreateExecuteForEachEntryOf(key, connector);

		return getOrCreateCustomConcatMethod(executeForEachEntryOf);
	}

	/**
	 * Get or create the custom concatMethod in the given executeForEachEntryOf node
	 *
	 * @param executeForEachEntryOf The {@link ObjectNode} instance
	 * @return {@link ObjectNode} instance
	 */
	protected ObjectNode getOrCreateCustomConcatMethod(final ObjectNode executeForEachEntryOf) {
		JsonNode concatMethod = executeForEachEntryOf.get(CONCAT_METHOD);

		if (concatMethod == null) {
			concatMethod = JsonNodeFactory.instance.objectNode();
			executeForEachEntryOf.set(CONCAT_METHOD, concatMethod);
			return (ObjectNode) concatMethod;
		}

		return (ObjectNode) concatMethod;
	}
}
