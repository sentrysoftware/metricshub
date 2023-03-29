package com.sentrysoftware.matrix.converter.state;

import static com.sentrysoftware.matrix.converter.ConverterConstants.MONITORS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.ConverterConstants;
import com.sentrysoftware.matrix.converter.PreConnector;

public class CollectTypeProcessor extends AbstractStateConverter {

	private static final Pattern PATTERN = Pattern.compile("^\\s*(([a-z]+)\\.(collect)\\.(type))\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null && key != null && PATTERN.matcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format("Invalid key: %s", key));
		}
		final ObjectNode collectJob = getOrCreateMonitorCollectJob(matcher, connector);

		appendComment(key, preConnector, collectJob);

		value = ConverterConstants.MONO_INSTANCE.equalsIgnoreCase(value) ? ConverterConstants.MONO_INSTANCE_CAMEL_CASE
				: ConverterConstants.MULTI_INSTANCE_CAMEL_CASE;

		createTextNode("type", value, collectJob);
	}

	/**
	 * Create collect job for the given monitor (and create the monitor if not
	 * exists)
	 * 
	 * @param matcher
	 * @param connector
	 * @return
	 */
	private ObjectNode getOrCreateMonitorCollectJob(Matcher matcher, JsonNode connector) {
		final JsonNode monitors = connector.get(MONITORS);
		final String monitorName = getMonitorName(matcher);
		final String jobName = "collect";
		final ObjectNode collectJob = JsonNodeFactory.instance.objectNode();

		if (monitors == null) {
			// Create the whole hierarchy
			((ObjectNode) connector).set(MONITORS, JsonNodeFactory.instance.objectNode().set(monitorName,
					JsonNodeFactory.instance.objectNode().set(jobName, collectJob)));
			return collectJob;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			((ObjectNode) monitors).set(monitorName, JsonNodeFactory.instance.objectNode().set(jobName, collectJob));
			return collectJob;
		}

		// or get the job
		return (ObjectNode) monitor.get(jobName);

	}

	@Override
	protected Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

}
