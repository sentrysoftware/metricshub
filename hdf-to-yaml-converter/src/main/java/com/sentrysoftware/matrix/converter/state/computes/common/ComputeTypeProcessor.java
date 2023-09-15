package com.sentrysoftware.matrix.converter.state.computes.common;

import static com.sentrysoftware.matrix.converter.ConverterConstants.COMPUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MONITORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.SOURCES;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.PreConnector;
import com.sentrysoftware.matrix.converter.state.AbstractStateConverter;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ComputeTypeProcessor extends AbstractStateConverter {

	@Getter
	private final String hdfType;

	@Getter
	private final String yamlType;

	private static final Pattern PATTERN = Pattern.compile(
		ConversionHelper.buildComputeKeyRegex("type"),
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

		final ObjectNode compute = createCompute(matcher, connector);

		appendComment(key, preConnector, compute);

		createTextNode("type", yamlType, compute);
	}

	/**
	 * Create a compute node in the given connector
	 *
	 * @param matcher The matcher used to determine the monitor name, the job
	 * name and the source name
	 * @param connector The global connector node
	 * @return {@link ObjectNode} instance
	 */
	private ObjectNode createCompute(final Matcher matcher, final JsonNode connector) {
		final JsonNode monitors = connector.get(MONITORS);
		final ObjectNode compute = JsonNodeFactory.instance.objectNode();
		final String sourceName = getSourceName(matcher);
		final String jobName = getMonitorJobName(matcher);
		final String monitorName = getMonitorName(matcher);

		if (monitors == null) {
			// Create the whole hierarchy
			((ObjectNode) connector).set(
					MONITORS,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							monitorName,
							JsonNodeFactory.instance
								.objectNode()
								.set(
									jobName,
									JsonNodeFactory.instance
										.objectNode()
										.set(
											SOURCES,
											JsonNodeFactory.instance
												.objectNode()
												.set(
													sourceName,
													JsonNodeFactory.instance
														.objectNode()
														.set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute))
												)
										)
								)
						)
				);
			return compute;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			((ObjectNode) monitors).set(
					monitorName,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							jobName,
							JsonNodeFactory.instance
								.objectNode()
								.set(
									SOURCES,
									JsonNodeFactory.instance
										.objectNode()
										.set(
											sourceName,
											JsonNodeFactory.instance
												.objectNode()
												.set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute))
										)
								)
						)
				);
			return compute;
		}

		// Check the job
		final JsonNode job = monitor.get(jobName);
		if (job == null) {
			((ObjectNode) monitor).set(
					jobName,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							SOURCES,
							JsonNodeFactory.instance
								.objectNode()
								.set(
									sourceName,
									JsonNodeFactory.instance.objectNode().set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute))
								)
						)
				);
			return compute;
		}

		// Check the sources
		final JsonNode sources = job.get(SOURCES);
		if (sources == null) {
			((ObjectNode) job).set(
					SOURCES,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							sourceName,
							JsonNodeFactory.instance.objectNode().set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute))
						)
				);
			return compute;
		}

		// Check the source
		final JsonNode source = sources.get(sourceName);
		if (source == null) {
			((ObjectNode) sources).set(
					sourceName,
					JsonNodeFactory.instance.objectNode().set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute))
				);

			return compute;
		}

		// Check the computes node
		final ArrayNode computes = (ArrayNode) source.get(COMPUTES);

		// This is the first compute we have encountered
		if (computes == null) {
			((ObjectNode) source).set(COMPUTES, JsonNodeFactory.instance.arrayNode().add(compute));
			return compute;
		}

		computes.add(compute);

		return compute;
	}
}
