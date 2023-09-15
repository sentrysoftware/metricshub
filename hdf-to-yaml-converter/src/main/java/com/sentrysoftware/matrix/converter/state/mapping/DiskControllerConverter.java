package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_BIOS_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CONTROLLER_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CONTROLLER_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DRIVER_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_BIOS_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CONTROLLER_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISK_CONTROLLER_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DRIVER_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FIRMWARE_VERSION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DiskControllerConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_CONTROLLER_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_CONTROLLER_NUMBER));
		attributesMap.put(HDF_BIOS_VERSION, IMappingKey.of(ATTRIBUTES, YAML_BIOS_VERSION));
		attributesMap.put(HDF_FIRMWARE_VERSION, IMappingKey.of(ATTRIBUTES, YAML_FIRMWARE_VERSION));
		attributesMap.put(HDF_DRIVER_VERSION, IMappingKey.of(ATTRIBUTES, YAML_DRIVER_VERSION));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_DISK_CONTROLLER_STATUS));
		metricsMap.put(HDF_CONTROLLER_STATUS, IMappingKey.of(METRICS, YAML_DISK_CONTROLLER_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
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
		JsonNode deviceId = existingAttributes.get(HDF_DEVICE_ID);
		if (deviceId == null) {
			return;
		}

		final JsonNode displayId = existingAttributes.get(HDF_DISPLAY_ID);
		if (displayId != null) {
			deviceId = displayId;
		} else {
			final JsonNode controllerNumber = existingAttributes.get(HDF_CONTROLLER_NUMBER);
			if (controllerNumber != null) {
				deviceId = controllerNumber;
			}
		}

		final JsonNode vendor = existingAttributes.get(HDF_VENDOR);
		final JsonNode model = existingAttributes.get(HDF_MODEL);

		newAttributes.set(YAML_NAME, new TextNode(wrapInAwkRefIfFunctionDetected(buildNameValue(deviceId, vendor, model))));
	}

	/**
	 * Joins the given non-empty text nodes to build the disk controller name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name, never null
	 * @param vendorAndModel       {@link JsonNode} array representing vendor and model
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode... vendorAndModel) {
		final String firstArg = firstDisplayArgument.asText();

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"Disk Controller: %s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(Stream.of(vendorAndModel).filter(Objects::nonNull).map(JsonNode::asText).toList());

		// Means vendor or model is not null
		if (!sprintfArgs.isEmpty()) {
			format.append(sprintfArgs.stream().map(v -> "%s").collect(Collectors.joining(" ", " (", ")")));
		}

		// Add the first argument at the beginning of the list
		sprintfArgs.add(0, firstArg);

		// Join the arguments, example: $column(1), $column(2), $column(3)
		// Append the result to our format variable in order to get something like
		// sprintf("Disk Controller: %s (%s %s)", $column(1), $column(2), $column(3))
		return format
			.append("\", ") // Here we will have a string like sprintf("Disk Controller: %s (%s %s)",
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
