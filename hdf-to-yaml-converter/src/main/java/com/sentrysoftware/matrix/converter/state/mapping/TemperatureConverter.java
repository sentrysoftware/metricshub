package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TEMPERATURE_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TEMPERATURE_VALUE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_SENSOR_LOCATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TEMPERATURE_LIMIT_CRITICAL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TEMPERATURE_LIMIT_DEGRADED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TEMPERATURE_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TEMPERATURE_VALUE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class TemperatureConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_TEMPERATURE_TYPE, IMappingKey.of(ATTRIBUTES, YAML_SENSOR_LOCATION));
		attributesMap.put(HDF_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_TEMPERATURE_LIMIT_DEGRADED));
		attributesMap.put(HDF_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_TEMPERATURE_LIMIT_CRITICAL));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_TEMPERATURE_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_TEMPERATURE_VALUE, IMappingKey.of(METRICS, YAML_TEMPERATURE_VALUE));
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

		final JsonNode temperatureType = existingAttributes.get(HDF_TEMPERATURE_TYPE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				wrapInAwkRefIfFunctionDetected(
					buildNameValue(firstDisplayArgument, temperatureType)
				)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the temperature name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param temperatureTypeNode       {@link JsonNode} temperature type
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode temperatureTypeNode) {

		final String firstArg = firstDisplayArgument.asText();
		if (temperatureTypeNode == null) {
			return firstArg;
		}

		return new StringBuilder("sprintf(\"%s (%s)\", ")
			.append(getFunctionArgument(firstArg))
			.append(", ")
			.append(getFunctionArgument(temperatureTypeNode.asText()))
			.append(")")
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
