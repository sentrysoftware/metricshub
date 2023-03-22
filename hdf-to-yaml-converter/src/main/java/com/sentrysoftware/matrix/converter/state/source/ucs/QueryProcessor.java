package com.sentrysoftware.matrix.converter.state.source.ucs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public class QueryProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile(
			ConversionHelper.buildSourceKeyRegex("query[1-9]\\d*"),
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final ArrayNode queries = getOrCreateQueries(key, connector);
		queries.add(ConversionHelper.performValueConversions(value));
	}

	/**
	 * Get or create the queries {@link ArrayNode}
	 * 
	 * @param key       The key used to extract the source
	 * @param connector The global connector object
	 * @return {@link ArrayNode} instance
	 */
	protected ArrayNode getOrCreateQueries(final String key, final JsonNode connector) {
		final ObjectNode source = getCurrentSource(key, connector);

		JsonNode queries = source.get("queries");

		if (queries == null) {
			queries = JsonNodeFactory.instance.arrayNode();
			source.set("queries", queries);
			return (ArrayNode) queries;
		}

		return (ArrayNode) queries;
	}
}