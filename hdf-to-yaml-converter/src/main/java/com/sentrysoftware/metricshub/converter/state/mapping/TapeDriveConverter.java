package com.sentrysoftware.metricshub.converter.state.mapping;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_ERROR_COUNT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_MOUNT_COUNT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_NEEDS_CLEANING;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_SERIAL_NUMBER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_UNMOUNT_COUNT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_SERIAL_NUMBER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_STATUS_INFORMATION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_ERRORS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_ERRORS_LIMIT_CRITICAL;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_ERRORS_LIMIT_DEGRADED;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_OPERATIONS_MOUNT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_OPERATIONS_UNMOUNT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_STATUS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_TAPE_DRIVE_STATUS_NEEDS_CLEANING;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VENDOR;
import static com.sentrysoftware.metricshub.converter.state.ConversionHelper.wrapInAwkRefIfFunctionDetected;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TapeDriveConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		attributesMap.put(
			HDF_ERROR_COUNT_WARNING_THRESHOLD,
			IMappingKey.of(METRICS, YAML_TAPE_DRIVE_ERRORS_LIMIT_DEGRADED)
		);
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
		metricsMap.put(
			HDF_NEEDS_CLEANING,
			IMappingKey.of(
				METRICS,
				YAML_TAPE_DRIVE_STATUS_NEEDS_CLEANING,
				AbstractMappingConverter::buildLegacyNeedsCleaningFunction
			)
		);
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

		final JsonNode vendor = existingAttributes.get(HDF_VENDOR);
		final JsonNode model = existingAttributes.get(HDF_MODEL);

		newAttributes.set(
			YAML_NAME,
			new TextNode(wrapInAwkRefIfFunctionDetected(buildNameValue(firstDisplayArgument, vendor, model)))
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the tape drive value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param info                 {@link JsonNode} array to be joined. Actually contains vendor and model
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode... info) {
		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(info).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(Stream.of(info).filter(Objects::nonNull).map(JsonNode::asText).toList());

		// Means we have at least one value in info (vendor, model or both)
		if (!sprintfArgs.isEmpty()) {
			format.append(sprintfArgs.stream().map(v -> "%s").collect(Collectors.joining(" ", " (", ")")));
		}

		// Add the first argument at the beginning of the list
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $1, $2, $3)
		// append the result to our format variable in order to get something like
		// sprint("%s (%s %s)", $1, $2, $3)
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s %s)",
			.append(sprintfArgs.stream().map(this::getFunctionArgument).collect(Collectors.joining(", ", "", ")")))
			.toString();
	}

	@Override
	protected Map<String, Entry<String, IMappingKey>> getOneToOneMetricsMapping() {
		return ONE_TO_ONE_METRICS_MAPPING;
	}

	@Override
	public void convertCollectProperty(final String key, final String value, final JsonNode node) {
		convertOneToOneMetrics(key, value, (ObjectNode) node);
	}
}
