package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SIZE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ERROR_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ERROR_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MEMORY_ERRORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MEMORY_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MEMORY_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MEMORY_LIMIT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VENDOR;

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

public class MemoryConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_TYPE, IMappingKey.of(ATTRIBUTES, YAML_TYPE));
		attributesMap.put(HDF_SIZE, IMappingKey.of(METRICS, YAML_MEMORY_LIMIT, AbstractMappingConverter::buildMebiByte2ByteFunction));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		attributesMap.put(HDF_ERROR_COUNT_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_ERROR_COUNT_ALARM_THRESHOLD));
		attributesMap.put(HDF_ERROR_COUNT_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_ERROR_COUNT_WARNING_THRESHOLD));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_MEMORY_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_PREDICTED_FAILURE, IMappingKey.of(METRICS, YAML_MEMORY_PREDICTED_FAILURE, AbstractMappingConverter::buildBooleanFunction));
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_MEMORY_ERRORS));
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

		final JsonNode vendor = existingAttributes.get(HDF_VENDOR);
		final JsonNode model = existingAttributes.get(HDF_MODEL);
		final JsonNode type = existingAttributes.get(HDF_TYPE);
		final JsonNode size = existingAttributes.get(HDF_SIZE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				buildNameValue(firstDisplayArgument, new JsonNode[] {vendor, model}, type, size)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the memory name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendorAndModel       {@link JsonNode[]} array of vendor and model to be joined 
	 * @param typeNode             {@link JsonNode} representing the type of the memory
	 * @param size 
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] vendorAndModel, final JsonNode typeNode, JsonNode sizeNode) {

		final String firstArg = firstDisplayArgument.asText();
		if (typeNode == null && sizeNode == null && Stream.of(vendorAndModel).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(
			Stream
				.of(vendorAndModel)
				.filter(Objects::nonNull)
				.map(JsonNode::asText)
				.toList()
		);

		// Means we have model or vendor but we don't know if have the type
		if (sprintfArgs.size() == 1) {
			format.append(" (%s");
		} else if (sprintfArgs.size() == 2) {
			// We have both model and vendor but we don't know if we have the type
			format.append(" (%s (%s)");
		}

		// Do we have the type?
		if (typeNode != null) {

			// Without vendor and model?
			format.append(sprintfArgs.isEmpty() ? " (%s" : " - %s");

			// Add the type to our list of arguments
			sprintfArgs.add(typeNode.asText());

		}

		// Do we have the size?
		if (sizeNode != null) {

			// Without vendor, model or type?
			format.append(sprintfArgs.isEmpty() ? " (%s)" : " - %s)");

			// Add the type to our list of arguments
			sprintfArgs.add(sizeNode.asText());

		} else if (!sprintfArgs.isEmpty()) {
			// We have at least one of { vendor, model, type, size } let's close the parenthesis
			format.append(")");
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3), $column(4)) 
		// append the result to our format variable in order to get something like
		// sprint("%s (%s %s - %s)", $column(1), $column(2), $column(3), $column(4))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s %s - %s)", 
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
		convertOneToOneMetrics(key, value, (ObjectNode) node);
	}

}