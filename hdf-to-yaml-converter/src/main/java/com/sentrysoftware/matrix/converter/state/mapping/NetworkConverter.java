package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DUPLEX_MODE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LINK_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LINK_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LOGICAL_ADDRESS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LOGICAL_ADDRESS_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PHYSICAL_ADDRESS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PHYSICAL_ADDRESS_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_RECEIVED_BYTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_RECEIVED_PACKETS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TRANSMITTED_BYTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TRANSMITTED_PACKETS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ZERO_BUFFER_CREDIT_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DEVICE_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LOGICAL_ADDRESS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LOGICAL_ADDRESS_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_BANDWIDTH_LIMIT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_ERRORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_ERROR_ZERO_BUFFER_CREDIT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_FULL_DUPLEX;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_RECEIVED_BYTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_RECEIVED_PACKETS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_TRANSMITTED_BYTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_TRANSMITTED_PACKETS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK_UP;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_ADDRESS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_ADDRESS_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_SERIAL_NUMBER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;

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

public class NetworkConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_DEVICE_TYPE, IMappingKey.of(ATTRIBUTES, YAML_DEVICE_TYPE));
		attributesMap.put(HDF_PHYSICAL_ADDRESS, IMappingKey.of(ATTRIBUTES, YAML_PHYSICAL_ADDRESS));
		attributesMap.put(HDF_PHYSICAL_ADDRESS_TYPE, IMappingKey.of(ATTRIBUTES, YAML_PHYSICAL_ADDRESS_TYPE));
		attributesMap.put(HDF_LOGICAL_ADDRESS, IMappingKey.of(ATTRIBUTES, YAML_LOGICAL_ADDRESS));
		attributesMap.put(HDF_LOGICAL_ADDRESS_TYPE, IMappingKey.of(ATTRIBUTES, YAML_LOGICAL_ADDRESS_TYPE));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_NETWORK_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_LINK_STATUS, IMappingKey.of(METRICS, YAML_NETWORK_UP, AbstractMappingConverter::buildLegacyLinkFunction));
		metricsMap.put(HDF_DUPLEX_MODE, IMappingKey.of(METRICS, YAML_NETWORK_FULL_DUPLEX, AbstractMappingConverter::buildLegacyFullDuplexFunction));
		metricsMap.put(HDF_ZERO_BUFFER_CREDIT_COUNT, IMappingKey.of(METRICS, YAML_NETWORK_ERROR_ZERO_BUFFER_CREDIT));
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_NETWORK_ERRORS));
		metricsMap.put(HDF_RECEIVED_BYTES, IMappingKey.of(METRICS, YAML_NETWORK_RECEIVED_BYTES));
		metricsMap.put(HDF_TRANSMITTED_BYTES, IMappingKey.of(METRICS, YAML_NETWORK_TRANSMITTED_BYTES));
		metricsMap.put(HDF_RECEIVED_PACKETS, IMappingKey.of(METRICS, YAML_NETWORK_RECEIVED_PACKETS));
		metricsMap.put(HDF_TRANSMITTED_PACKETS, IMappingKey.of(METRICS, YAML_NETWORK_TRANSMITTED_PACKETS));
		metricsMap.put(HDF_LINK_SPEED, IMappingKey.of(METRICS, YAML_NETWORK_BANDWIDTH_LIMIT, AbstractMappingConverter::buildMegaBit2BitFunction));
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

		final JsonNode deviceType = existingAttributes.get(HDF_DEVICE_TYPE);
		final JsonNode model = existingAttributes.get(HDF_MODEL);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				buildNameValue(firstDisplayArgument, new JsonNode[] {deviceType, model})
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the battery name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param deviceTypeAndModel   {@link JsonNode[]} array of vendor and model to be joined 
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] deviceTypeAndModel) {

		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(deviceTypeAndModel).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(
			Stream
				.of(deviceTypeAndModel)
				.filter(Objects::nonNull)
				.map(JsonNode::asText)
				.toList()
		);

		// Means we have model or vendor but we don't know if have the type
		if (sprintfArgs.size() == 1) {
			format.append(" (%s)");
		} else if (sprintfArgs.size() == 2) {
			// We have both model and vendor but we don't know if we have the type
			format.append(" (%s - %s)");
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3)) 
		// append the result to our format variable in order to get something like
		// sprint("%s (%s - %s)", $column(1), $column(2), $column(3))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s)", 
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
			final JsonNode linkStatus = metrics.get(YAML_NETWORK_UP);
			if (linkStatus != null && !linkStatus.asText().contains("legacyLinkStatus")) {
				((ObjectNode) metrics).set(
						YAML_NETWORK_UP,
						new TextNode(
						buildLegacyLinkFunction(
							getFunctionArgument(linkStatus.asText())
						)
					)
				);
			}

			final JsonNode duplexMode = metrics.get(YAML_NETWORK_FULL_DUPLEX);
			if (duplexMode != null && !duplexMode.asText().contains("legacyFullDuplex")) {
				((ObjectNode) metrics).set(
						YAML_NETWORK_FULL_DUPLEX,
						new TextNode(
						buildLegacyFullDuplexFunction(
							getFunctionArgument(duplexMode.asText())
						)
					)
				);
			}
		}
	}

}
