package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_NOT_READY_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ENDURANCE_REMAINING;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_HARD_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ILLEGAL_REQUEST_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MEDIA_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_NO_DEVICE_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_RECOVERABLE_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SIZE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TRANSPORT_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ENDURANCE_UTILIZATION_REMAINING;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_DEVICE_NOT_READY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_HARD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_ILLEGAL_REQUEST;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_MEDIA;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_NO_DEVICE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_RECOVERABLE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_ERRORS_TRANSPORT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_SIZE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK_STATUS_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VENDOR;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.wrapInAwkRefIfFunctionDetected;

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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PhysicalDiskConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_SIZE, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_SIZE));
		attributesMap.put(HDF_FIRMWARE_VERSION, IMappingKey.of(ATTRIBUTES, YAML_FIRMWARE_VERSION));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(
			HDF_PREDICTED_FAILURE,
			IMappingKey.of(
				METRICS,
				YAML_PHYSICAL_DISK_STATUS_PREDICTED_FAILURE,
				AbstractMappingConverter::buildBooleanFunction
			)
		);
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS));
		metricsMap.put(HDF_TRANSPORT_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_TRANSPORT));
		metricsMap.put(HDF_ILLEGAL_REQUEST_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_ILLEGAL_REQUEST));
		metricsMap.put(HDF_NO_DEVICE_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_NO_DEVICE));
		metricsMap.put(
			HDF_DEVICE_NOT_READY_ERROR_COUNT,
			IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_DEVICE_NOT_READY)
		);
		metricsMap.put(HDF_RECOVERABLE_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_RECOVERABLE));
		metricsMap.put(HDF_HARD_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_HARD));
		metricsMap.put(HDF_MEDIA_ERROR_COUNT, IMappingKey.of(METRICS, YAML_PHYSICAL_DISK_ERRORS_MEDIA));
		metricsMap.put(
			HDF_ENDURANCE_REMAINING,
			IMappingKey.of(
				METRICS,
				YAML_PHYSICAL_DISK_ENDURANCE_UTILIZATION_REMAINING,
				AbstractMappingConverter::buildPercent2RatioFunction
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
		final JsonNode size = existingAttributes.get(HDF_SIZE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(wrapInAwkRefIfFunctionDetected(buildNameValue(firstDisplayArgument, vendor, size)))
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the physical disk name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendor               {@link JsonNode} representing the physical disk's vendor
	 * @param size                 {@link JsonNode} representing the physical disk's size
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode vendor, final JsonNode size) {
		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(vendor, size).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();

		// Means vendor or size is not null
		if (vendor != null || size != null) {
			format.append(
				Stream
					.concat(
						Optional
							.ofNullable(vendor)
							.map(v -> {
								sprintfArgs.add(v.asText());
								return "%s";
							})
							.stream(),
						Optional
							.ofNullable(size)
							.map(v -> {
								sprintfArgs.add(String.format("bytes2HumanFormatBase10(%s)", v.asText()));
								return "%s"; // Bytes to human format using base 10 conversion
							})
							.stream()
					)
					.collect(Collectors.joining(" - ", " (", ")"))
			);
		}

		// Add the first argument at the beginning of the list
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $1, $2, $3)
		// append the result to our format variable in order to get something like
		// sprintf("%s (%s - %s)", $1, $2, bytes2HumanFormatBase10($3))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s - %s)",
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

	@Override
	protected String getFunctionArgument(String value) {
		// It is not required to concatenated the value with the opening and closing quotation marks
		if (value.indexOf("bytes2HumanFormatBase10") != -1) {
			return value;
		}
		return super.getFunctionArgument(value);
	}
}
