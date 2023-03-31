package com.sentrysoftware.matrix.converter.state;

import static com.sentrysoftware.matrix.converter.ConverterConstants.COLLECT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MONITORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MONO_INSTANCE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MONO_INSTANCE_CAMEL_CASE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MULTI_INSTANCE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MULTI_INSTANCE_CAMEL_CASE;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;

public class CollectTypeProcessor extends AbstractStateConverter {

	private static final Map<String, String> COLLECT_TYPE_MAP = Map.of(
		MONO_INSTANCE, MONO_INSTANCE_CAMEL_CASE,
		MULTI_INSTANCE, MULTI_INSTANCE_CAMEL_CASE
	);

	private static final Pattern PATTERN = Pattern.compile(
		"^\\s*(([a-z]+)\\.(collect)\\.(type))\\s*$",
		Pattern.CASE_INSENSITIVE
	);


	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null
			&& key != null
			&& getMatcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format("Invalid key: %s", key));
		}
		final ObjectNode collectJob = getOrCreateMonitorCollectJob(matcher, connector);

		appendComment(key, preConnector, collectJob);

		final String collectype = COLLECT_TYPE_MAP.get(value.toLowerCase());

		if (collectype == null) {
			throw new IllegalStateException(String.format("Unknown collect type: %s", value));
		}

		createTextNode("type", collectype, collectJob);
	}

	/**
	 * Create collect job for the given monitor (and create the correct hierarchy if not
	 * exists)
	 * 
	 * @param matcher
	 * @param connector
	 * @return {@link ObjectNode} instance
	 */
	private ObjectNode getOrCreateMonitorCollectJob(final Matcher matcher, final JsonNode connector) {
		final JsonNode monitors = connector.get(MONITORS);
		final String monitorName = getMonitorName(matcher);

		final ObjectNode collectJob = JsonNodeFactory.instance.objectNode();

		if (monitors == null) {
			// Create the whole hierarchy
			((ObjectNode) connector)
				.set(
					MONITORS,
					JsonNodeFactory.instance.objectNode()
						.set(
							monitorName,
							JsonNodeFactory.instance.objectNode()
								.set(COLLECT, collectJob)
						)
				);
			return collectJob;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			((ObjectNode) monitors)
				.set(
					monitorName,
					JsonNodeFactory.instance.objectNode()
						.set(COLLECT, collectJob)
				);
			return collectJob;
		}

		// Check if the job has been created
		JsonNode jobObjectNode = monitor.get(COLLECT);
		if (jobObjectNode == null) {
			((ObjectNode) monitors).set(COLLECT, collectJob);
			return collectJob;
		}

		// Otherwise a collect already exists for this monitor, so return the object
		return (ObjectNode) monitor.get(COLLECT);
	}

}
