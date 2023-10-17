package com.sentrysoftware.metricshub.converter.state.mapping;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_POWER_SUPPLY_POWER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_POWER_SUPPLY_TYPE;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_USED_PERCENT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_USED_WATTS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_POWER_SUPPLY_LIMIT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_POWER_SUPPLY_POWER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_POWER_SUPPLY_STATUS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_POWER_SUPPLY_TYPE;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_POWER_SUPPLY_UTILIZATION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_STATUS_INFORMATION;
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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
		metricsMap.put(
			HDF_USED_PERCENT,
			IMappingKey.of(METRICS, YAML_POWER_SUPPLY_UTILIZATION, AbstractMappingConverter::buildPercent2RatioFunction)
		);
		metricsMap.put(HDF_USED_WATTS, IMappingKey.of(METRICS, YAML_POWER_SUPPLY_POWER));

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

		final JsonNode type = existingAttributes.get(HDF_POWER_SUPPLY_TYPE);
		final JsonNode power = existingAttributes.get(HDF_POWER_SUPPLY_POWER);

		newAttributes.set(
			YAML_NAME,
			new TextNode(wrapInAwkRefIfFunctionDetected(buildNameValue(firstDisplayArgument, type, power)))
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the disk name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param type                 {@link JsonNode} representing the kind of the power supply
	 * @param power                {@link JsonNode} representing the actual power supply power
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode type, final JsonNode power) {
		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(type, power).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();

		// Means type or power is not empty
		if (type != null || power != null) {
			format.append(
				Stream
					.concat(
						Optional
							.ofNullable(type)
							.map(v -> {
								sprintfArgs.add(v.asText());
								return "%s";
							})
							.stream(),
						Optional
							.ofNullable(power)
							.map(v -> {
								sprintfArgs.add(v.asText());
								return "%s W";
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
		// sprint("%s (%s - %s W)", $1, $2, $3)
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s W)",
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
			final JsonNode power = metrics.get(YAML_POWER_SUPPLY_POWER);

			if (power != null && !metrics.has(YAML_POWER_SUPPLY_UTILIZATION)) {
				((ObjectNode) metrics).set(
						YAML_POWER_SUPPLY_UTILIZATION,
						new TextNode(buildLegacyPowerSupplyUtilizationFunction(power.asText()))
					);
			}
		}
	}
}
