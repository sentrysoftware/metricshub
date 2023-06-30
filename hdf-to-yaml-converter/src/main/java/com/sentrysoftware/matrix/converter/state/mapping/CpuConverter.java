package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CORRECTED_ERROR_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CORRECTED_ERROR_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CORRECTED_ERROR_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CURRENT_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MAX_POWER_CONSUMPTION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_POWER_CONSUMPTION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_ENERGY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_ERRORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_ERRORS_LIMIT_CRITICAL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_ERRORS_LIMIT_DEGRADED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_POWER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_POWER_LIMIT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_PREDICTED_FAILURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_SPEED_LIMIT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VENDOR;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.*;

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

public class CpuConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_MAX_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_CPU_POWER_LIMIT));
		attributesMap.put(HDF_MAXIMUM_SPEED, IMappingKey.of(METRICS, YAML_CPU_SPEED_LIMIT, AbstractMappingConverter::buildMegaHertz2HertzFunction));
		attributesMap.put(HDF_CORRECTED_ERROR_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_CPU_ERRORS_LIMIT_DEGRADED));
		attributesMap.put(HDF_CORRECTED_ERROR_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_CPU_ERRORS_LIMIT_CRITICAL));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_CPU_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_PREDICTED_FAILURE, IMappingKey.of(METRICS, YAML_CPU_PREDICTED_FAILURE, AbstractMappingConverter::buildBooleanFunction));
		metricsMap.put(HDF_CURRENT_SPEED, IMappingKey.of(METRICS, YAML_CPU_SPEED, AbstractMappingConverter::buildMegaHertz2HertzFunction));
		metricsMap.put(HDF_CORRECTED_ERROR_COUNT, IMappingKey.of(METRICS, YAML_CPU_ERRORS));
		metricsMap.put(HDF_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_CPU_POWER));
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
		final JsonNode maximumSpeed = existingAttributes.get(HDF_MAXIMUM_SPEED);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				wrapInAwkRefIfFunctionDetected(
					buildNameValue(firstDisplayArgument, new JsonNode[] { vendor, model }, maximumSpeed)
				)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the CPU name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendorAndModel       {@link JsonNode} array of vendor and model to be joined 
	 * @param maximumSpeed         {@link JsonNode} representing the max speed in MHz of the CPU
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(
		final JsonNode firstDisplayArgument,
		final JsonNode[] vendorAndModel,
		final JsonNode maximumSpeed
	) {

		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(vendorAndModel).allMatch(Objects::isNull) && maximumSpeed == null) {
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

		// Means vendor, model or maximumSpeed is not empty
		if (!sprintfArgs.isEmpty() || maximumSpeed != null) {
			format.append(
					Stream.concat(
						sprintfArgs
							.stream()
							.map(v -> "%s"),
						Stream.of(maximumSpeed)
							.filter(Objects::nonNull)
							.map(v -> {
								sprintfArgs.add(String.format("megaHertz2HumanFormat(%s)", v.asText()));
								return "%s"; // Mega Hertz to human format
							})
					)
					.collect(Collectors.joining(" - "," (",")"))
			);
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3), $column(4)) 
		// append the result to our format variable in order to get something like
		// sprint("%s (%s - %s - %s)", $column(1), $column(2), $column(3), megaHertz2HumanFormat($column(4)))
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s - %s)", 
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
			final JsonNode powerConsumption = metrics.get(YAML_CPU_POWER);
			if (powerConsumption != null) {
				((ObjectNode) metrics).set(
					YAML_CPU_ENERGY,
					new TextNode(
						buildFakeCounterFunction(
							getFunctionArgument(
								powerConsumption.asText()
							)
						)
					)
				);
			}
		}
	}

	@Override
	protected String getFunctionArgument(String value) {
		// It is not required to concatenated the value with the opening and closing quotation marks 
		if (value.indexOf("megaHertz2HumanFormat") != -1) {
			return value;
		}
		return super.getFunctionArgument(value);
	}
}
