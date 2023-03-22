package com.sentrysoftware.matrix.converter.state.source.common;

import static com.sentrysoftware.matrix.converter.ConverterConstants.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class TypeProcessor extends AbstractStateConverter {

	@Getter
	private final String hdfType;
	@Getter
	private final String yamlType;

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildSourceKeyRegex("type"),
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public Matcher getMatcher(String key) {
		return PATTERN.matcher(key);
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format("Invalid key: %s", key));
		}
		final ObjectNode source = createSource(matcher, connector);

		appendComment(key, preConnector, source);

		createTextNode("type", yamlType, source);
	}

	/**
	 * Create a source node in the given connector
	 * 
	 * @param matcher The matcher used to determine the monitor name, the job
	 * name and the source name
	 * @param connector The global connector node
	 * @return {@link ObjectNode} instance
	 */
	private ObjectNode createSource(final Matcher matcher, final JsonNode connector) {

		final JsonNode monitors = connector.get(MONITORS);
		final ObjectNode source = JsonNodeFactory.instance.objectNode();
		final String sourceName = getSourceName(matcher);
		final String jobName = getMonitorJobName(matcher);
		final String monitorName = getMonitorName(matcher);

		if (monitors == null) {
			// Create the whole hierarchy
			((ObjectNode)connector)
				.set(
					MONITORS,
					JsonNodeFactory.instance.objectNode()
						.set(
							monitorName,
							JsonNodeFactory.instance.objectNode()
								.set(
									jobName,
									JsonNodeFactory.instance.objectNode()
										.set(
											SOURCES,
											JsonNodeFactory.instance.objectNode()
												.set(sourceName, source)
										)
								)
						)
				);
			return source;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			((ObjectNode) monitors)
				.set(
					monitorName,
					JsonNodeFactory.instance.objectNode()
						.set(
							jobName,
							JsonNodeFactory.instance.objectNode()
								.set(
									SOURCES,
									JsonNodeFactory.instance.objectNode()
										.set(sourceName, source)
								)
						)
				);
			return source;
		}

		// Check the job
		final JsonNode job = monitor.get(jobName);
		if (job == null) {
			((ObjectNode) monitor)
				.set(
					jobName,
					JsonNodeFactory.instance.objectNode()
						.set(
							SOURCES,
							JsonNodeFactory.instance.objectNode()
								.set(sourceName, source)
						)
				);
			return source;
		}

		// Check the sources node
		final JsonNode sources = job.get(SOURCES);

		// At this level the sources node is never null
		if (sources == null) {
			throw new IllegalStateException("Sources cannot be null!");
		}

		((ObjectNode) sources).set(sourceName, source);

		return source;
	}

}
