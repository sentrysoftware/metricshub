package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.*;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
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
		attributesMap.put(HDF_RAID_LEVEL, IMappingKey.of(ATTRIBUTES, YAML_RAID_LEVEL));
		attributesMap.put(HDF_LOGICALDISK_TYPE, IMappingKey.of(ATTRIBUTES, YAML_TYPE));
		attributesMap.put(HDF_SIZE, IMappingKey.of(METRICS, YAML_LOGICALDISK_LIMIT));
		attributesMap.put(HDF_USE_FOR_CAPACITY_REPORT, IMappingKey.of(ATTRIBUTES, YAML_USE_FOR_CAPACITY_REPORT));
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
			return;
		}

		final JsonNode displayId = existingAttributes.get(HDF_DISPLAY_ID);
		JsonNode firstDisplayArgument = deviceId;
		if (displayId != null) {
			firstDisplayArgument = displayId;
		}

		final JsonNode raidLevel = existingAttributes.get(HDF_RAID_LEVEL);
		final JsonNode size = existingAttributes.get(HDF_SIZE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				wrapInAwkRefIfFunctionDetected(
					buildNameValue(firstDisplayArgument, raidLevel, size)
				)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the Logical Disk name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param raidLevel            {@link JsonNode} representing the raid level 
	 * @param size                 {@link JsonNode} representing the logical disk size
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(
		final JsonNode firstDisplayArgument,
		final JsonNode raidLevel,
		final JsonNode size
	) {

		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(raidLevel, size).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();

		// Means raid level or size is not empty
		if (raidLevel != null || size != null) {
			format.append(
					Stream.concat(
						Optional.ofNullable(raidLevel)
							.map(v -> {
								sprintfArgs.add(v.asText());
								return "%s";
							})
							.stream(),
						Optional.ofNullable(size)
							.map(v -> {
								sprintfArgs.add(String.format("bytes2HumanFormatBase2(%s)", v.asText()));
								return "%s"; // Bytes to human format using base 2 conversion
							})
							.stream()
					)
					.collect(Collectors.joining(" - "," (",")"))
			);
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3)) 
		// append the result to our format variable in order to get something like
		// sprintf("%s (%s - %s)", $column(1), $column(2), bytes2HumanFormatBase2($column(3)))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s -  %s)", 
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

	@Override
	protected String getFunctionArgument(String value) {
		// It is not required to concatenated the value with the opening and closing quotation marks 
		if (value.indexOf("bytes2HumanFormatBase2") != -1) {
			return value;
		}
		return super.getFunctionArgument(value);
	}
}
