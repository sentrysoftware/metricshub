package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ADDITIONAL_LABEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_POWER_CONSUMPTION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_USAGE_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_USAGE_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_USAGE_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VALUE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VALUE_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VALUE_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ADDITIONAL_LABEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DEVICE_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_ENERGY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_POWER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_USAGE_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_USAGE_COUNT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_USAGE_COUNT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_VALUE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_VALUE_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE_VALUE_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;
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
import java.util.stream.Collectors;

public class OtherDeviceConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_DEVICE_TYPE, IMappingKey.of(ATTRIBUTES, YAML_DEVICE_TYPE));
		attributesMap.put(HDF_ADDITIONAL_LABEL, IMappingKey.of(ATTRIBUTES, YAML_ADDITIONAL_LABEL));
		attributesMap.put(HDF_VALUE_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_VALUE_WARNING_THRESHOLD));
		attributesMap.put(HDF_VALUE_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_VALUE_ALARM_THRESHOLD));
		attributesMap.put(
			HDF_USAGE_COUNT_WARNING_THRESHOLD,
			IMappingKey.of(METRICS, YAML_OTHER_DEVICE_USAGE_COUNT_WARNING_THRESHOLD)
		);
		attributesMap.put(
			HDF_USAGE_COUNT_ALARM_THRESHOLD,
			IMappingKey.of(METRICS, YAML_OTHER_DEVICE_USAGE_COUNT_ALARM_THRESHOLD)
		);
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_POWER));
		metricsMap.put(HDF_USAGE_COUNT, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_USAGE_COUNT));
		metricsMap.put(HDF_VALUE, IMappingKey.of(METRICS, YAML_OTHER_DEVICE_VALUE));
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

		final JsonNode additionalLabel = existingAttributes.get(HDF_ADDITIONAL_LABEL);
		final JsonNode deviceType = existingAttributes.get(HDF_DEVICE_TYPE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(wrapInAwkRefIfFunctionDetected(buildNameValue(firstDisplayArgument, additionalLabel, deviceType)))
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the other device name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param additionalLabelNode  {@link JsonNode} representing the device additional label
	 * @param deviceTypeNode       {@link JsonNode} representing the device type
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(
		final JsonNode firstDisplayArgument,
		final JsonNode additionalLabelNode,
		final JsonNode deviceTypeNode
	) {
		final String firstArg = firstDisplayArgument.asText();
		if (additionalLabelNode == null && deviceTypeNode == null) {
			return firstArg;
		}

		// Create the empty function as the first argument can change
		final StringBuilder format = new StringBuilder("sprintf(\"");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();

		// Verify if have a deviceType
		if (deviceTypeNode != null) {
			format.append("%s: ");
			sprintfArgs.add(deviceTypeNode.asText());
		}

		// Add the ID
		format.append("%s");
		sprintfArgs.add(firstArg);

		// Add the additionalLabel if applicable
		if (additionalLabelNode != null) {
			format.append(" (%s)");
			sprintfArgs.add(additionalLabelNode.asText());
		}

		// Join the arguments: $column(1), $column(2), $column(3))
		// append the result to our format variable in order to get something like
		// sprint("%s: %s (%s)", $column(1), $column(2), $column(3))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s: %s (%s)",
			.append(sprintfArgs.stream().map(this::getFunctionArgument).collect(Collectors.joining(", ", "", ")")))
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
			final JsonNode powerConsumption = metrics.get(YAML_OTHER_DEVICE_POWER);
			if (powerConsumption != null) {
				((ObjectNode) metrics).set(
						YAML_OTHER_DEVICE_ENERGY,
						new TextNode(buildFakeCounterFunction(getFunctionArgument(powerConsumption.asText())))
					);
			}
		}
	}
}
