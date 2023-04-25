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

public class TapeDriveConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		attributesMap.put(HDF_ERROR_COUNT_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_ERRORS_LIMIT_DEGRADED));
		attributesMap.put(HDF_ERROR_COUNT_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_ERRORS_LIMIT_CRITICAL));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_ERRORS));
		metricsMap.put(HDF_MOUNT_COUNT, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_OPERATIONS_MOUNT));
		metricsMap.put(HDF_UNMOUNT_COUNT, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_OPERATIONS_UNMOUNT));
		metricsMap.put(HDF_NEEDS_CLEANING, IMappingKey.of(METRICS, YAML_TAPE_DRIVE_STATUS_NEEDS_CLEANING, AbstractMappingConverter::buildLegacyNeedsCleaningFunction));
		ONE_TO_ONE_METRICS_MAPPING = Collections.unmodifiableMap(metricsMap);
	}

	@Override
	protected Map<String, Entry<String, IMappingKey>> getOneToOneAttributesMapping() {
		return ONE_TO_ONE_ATTRIBUTES_MAPPING;
	}

	@Override
	protected void convertAttributesSpecific(JsonNode mapping, ObjectNode existingAttributes,
			ObjectNode newAttributes) {
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

		final JsonNode model = existingAttributes.get(HDF_MODEL);

		newAttributes.set(
				YAML_NAME,
				new TextNode(
						buildNameValue(firstDisplayArgument, deviceId, model)));
	}

	/**
	 * Joins the given non-empty text nodes to build the disk controller name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param displayId            {@link JsonNode} representing the displayId
	 * @param modelAndType         {@link JsonNode} representing the model
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode displayId,
			final JsonNode model) {

		final String firstArg = firstDisplayArgument.asText();
		if (displayId == null && model == null) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(
				Stream
						.of(model)
						.filter(Objects::nonNull)
						.map(JsonNode::asText)
						.toList());

		// Means model or size is not empty
		if (!sprintfArgs.isEmpty()) {
			format.append(
					sprintfArgs
							.stream()
							.map(v -> "%s")
							.collect(Collectors.joining(" - ", " (", ")\"")));
		}

		// Add the first argument at the beginning of the list
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3)
		// append the result to our format variable in order to get something like
		// sprint("%s: %s (%s)", $column(1), $column(2), $column(3))

		format
				.append(", ") // Here we will have a string like sprintf("%s %s (%s)"),
				.append(
						sprintfArgs
								.stream()
								.map(this::getFunctionArgument)
								.collect(Collectors.joining(", ", "", ")")))
				.toString();

		return format.toString();
	}

	@Override
	protected Map<String, Entry<String, IMappingKey>> getOneToOneMetricsMapping() {
		return ONE_TO_ONE_METRICS_MAPPING;
	}

	@Override
	public void convertCollectProperty(final String key, final String value, final JsonNode node) {
		final ObjectNode mapping = (ObjectNode) node;
		convertOneToOneMetrics(key, value, mapping);
	}
}
