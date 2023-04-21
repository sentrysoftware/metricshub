package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class LogicalDiskConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_RAID_LEVEL, IMappingKey.of(ATTRIBUTES, YAML_RAID_LEVEL));
		attributesMap.put(HDF_LOGICALDISK_TYPE, IMappingKey.of(ATTRIBUTES, HDF_TYPE));
		attributesMap.put(HDF_SIZE, IMappingKey.of(ATTRIBUTES, YAML_LOGICALDISK_LIMIT));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_LOGICALDISK_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_LOGICALDISK_ERRORS));
		metricsMap.put(HDF_UNALLOCATED_SPACE, IMappingKey.of(METRICS, YAML_LOGICALDISK_USAGE_FREE));

		
		ONE_TO_ONE_METRICS_MAPPING = Collections.unmodifiableMap(metricsMap);
	}

	@Override
	protected Map<String, Entry<String, IMappingKey>> getOneToOneAttributesMapping() {
		return ONE_TO_ONE_ATTRIBUTES_MAPPING;
	}

	@Override
	protected void convertAttributesSpecific(JsonNode mapping, ObjectNode existingAttributes, ObjectNode newAttributes) {
		// No specific attributes to convert
	}

	@Override
	protected void setName(ObjectNode existingAttributes, ObjectNode newAttributes) {
		final JsonNode deviceId = existingAttributes.get(HDF_DEVICE_ID);
		if (deviceId == null) {
			throw new IllegalStateException(String.format("%s cannot be null.", HDF_DEVICE_ID));
		}

		final JsonNode displayId = existingAttributes.get(HDF_DISPLAY_ID);
		JsonNode firstDisplayArgument = deviceId;
		if (displayId != null) {
			firstDisplayArgument = displayId;
		}

		final JsonNode raid = existingAttributes.get(HDF_RAID_LEVEL);
		final JsonNode size = existingAttributes.get(HDF_SIZE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				buildNameValue(firstDisplayArgument, new JsonNode[] {raid, size})
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the CPU name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param raidAndSize       {@link JsonNode[]} array of vendor and model to be joined 
	 * @param maximumSpeed         {@link JsonNode} representing the max speed in MHz of the CPU
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] raidAndSize) {

		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(raidAndSize).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(
			Stream
				.of(raidAndSize)
				.filter(Objects::nonNull)
				.map(JsonNode::asText)
				.toList()
		);

		// Means vendor, model or maximumSpeed is not empty
		if (!sprintfArgs.isEmpty()) {
			format.append(
				sprintfArgs
				.stream()
				.map(v -> "%s")
				.collect(Collectors.joining(" - "," (",")"))
			);
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3), $column(4)) 
		// append the result to our format variable in order to get something like
		// sprint("%s (%s - %s - %mhhf.s)", $column(1), $column(2), $column(3), $column(4))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s - %mhhf.s)", 
			.append(
				sprintfArgs
					.stream()
					.map(this::getFunctionArgument)
					.collect(Collectors.joining(", ", "", ")"))
			)
			.toString();

	}

	@Override
	protected Map<String, Entry<String, IMappingKey>> getOneToOneMetricsMapping() {
		return ONE_TO_ONE_METRICS_MAPPING;
	}

	@Override
	public void convertCollectProperty(final String key, final String value, final JsonNode node) {
		final ObjectNode mapping = (ObjectNode) node;
		convertOneToOneMetrics(key, value, mapping);

		final JsonNode metrics = mapping.get(METRICS);

		if (metrics != null) {
			final JsonNode free = metrics.get(YAML_LOGICALDISK_USAGE_FREE);
			if (free != null) {
				((ObjectNode) metrics).set(
					YAML_LOGICALDISK_USAGE_USED,
					new TextNode("collectAllocatedSpace()")
				);
			}
		}
	}
}
