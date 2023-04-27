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

public class PowerSupplyConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_POWER_SUPPLY_TYPE, IMappingKey.of(ATTRIBUTES, YAML_POWER_SUPPLY_TYPE));
		attributesMap.put(HDF_POWER_SUPPLY_POWER, IMappingKey.of(METRICS, YAML_POWER_SUPPLY_LIMIT));

		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_POWER_SUPPLY_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_USED_PERCENT, IMappingKey.of(METRICS, YAML_POWER_SUPPLY_UTILIZATION, AbstractMappingConverter::buildPercent2RatioFunction));
		metricsMap.put(HDF_USED_WATTS, IMappingKey.of(METRICS, YAML_POWER_SUPPLY_UTILIZATION));

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

		final JsonNode type = existingAttributes.get(HDF_POWER_SUPPLY_TYPE);
		final JsonNode power = existingAttributes.get(HDF_POWER_SUPPLY_POWER);

		newAttributes.set(
				YAML_NAME,
				new TextNode(
						buildNameValue(firstDisplayArgument, new JsonNode[] { type, power })));
	}

	/**
	 * Joins the given non-empty text nodes to build the disk controller name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param typeAndPower         {@link JsonNode} representing the type and power
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] typeAndPower) {

		final String firstArg = firstDisplayArgument.asText();
		if (typeAndPower == null) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(
				Stream
						.of(typeAndPower)
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

		final JsonNode metrics = mapping.get(METRICS);

		if (metrics != null) {
			final JsonNode power = metrics.get(YAML_POWER_SUPPLY_UTILIZATION);

			if (power != null && !power.asText().contains("percent2Ratio")) {
				((ObjectNode) metrics).set(
						YAML_POWER_SUPPLY_POWER,
						new TextNode(power.asText()));
				((ObjectNode) metrics).set(
						YAML_POWER_SUPPLY_UTILIZATION,
						new TextNode(buildLegacyPowerSupplyUtilizationFunction(power.asText())));
			}
		}
	}
}