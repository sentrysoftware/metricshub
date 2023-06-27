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

public class GpuConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		attributesMap.put(HDF_DRIVER_VERSION, IMappingKey.of(ATTRIBUTES, YAML_DRIVER_VERSION));
		attributesMap.put(HDF_FIRMWARE_VERSION, IMappingKey.of(ATTRIBUTES, YAML_FIRMWARE_VERSION));
		attributesMap.put(HDF_SIZE,
				IMappingKey.of(METRICS, YAML_GPU_MEMORY_LIMIT, AbstractMappingConverter::buildMebiByte2ByteFunction));
		attributesMap.put(HDF_MEMORY_UTILIZATION_WARNING_THRESHOLD, IMappingKey.of(METRICS,
				YAML_GPU_MEMORY_UTILIZATION_LIMIT_DEGRADED, AbstractMappingConverter::buildPercent2RatioFunction));
		attributesMap.put(HDF_MEMORY_UTILIZATION_ALARM_THRESHOLD, IMappingKey.of(METRICS,
				YAML_GPU_MEMORY_UTILIZATION_LIMIT_CRITICAL, AbstractMappingConverter::buildPercent2RatioFunction));
		attributesMap.put(HDF_USED_TIME_PERCENT_WARNING_THRESHOLD, IMappingKey.of(METRICS,
				YAML_GPU_UTILIZATION_LIMIT_DEGRADED, AbstractMappingConverter::buildPercent2RatioFunction));
		attributesMap.put(HDF_USED_TIME_PERCENT_ALARM_THRESHOLD, IMappingKey.of(METRICS,
				YAML_GPU_UTILIZATION_LIMIT_CRITICAL, AbstractMappingConverter::buildPercent2RatioFunction));

		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_GPU_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_PREDICTED_FAILURE, IMappingKey.of(METRICS, YAML_GPU_PREDICTED_FAILURE,
				AbstractMappingConverter::buildLegacyPredictedFailureFunction));
		metricsMap.put(HDF_CORRECTED_ERROR_COUNT, IMappingKey.of(METRICS, YAML_GPU_ERRORS_CORRECTED));
		metricsMap.put(HDF_ERROR_COUNT, IMappingKey.of(METRICS, YAML_GPU_ERRORS));
		metricsMap.put(HDF_USED_TIME_PERCENT, IMappingKey.of(METRICS, YAML_GPU_UTILIZATION_GENERAL,
				AbstractMappingConverter::buildPercent2RatioFunction));
		metricsMap.put(HDF_DECODER_USED_TIME_PERCENT, IMappingKey.of(METRICS, YAML_GPU_UTILIZATION_DECODER,
				AbstractMappingConverter::buildPercent2RatioFunction));
		metricsMap.put(HDF_ENCODER_USED_TIME_PERCENT, IMappingKey.of(METRICS, YAML_GPU_UTILIZATION_ENCODER,
				AbstractMappingConverter::buildPercent2RatioFunction));
		metricsMap.put(HDF_MEMORY_UTILIZATION, IMappingKey.of(METRICS, YAML_GPU_MEMORY_UTILIZATION,
				AbstractMappingConverter::buildPercent2RatioFunction));
		metricsMap.put(HDF_RECEIVED_BYTES, IMappingKey.of(METRICS, YAML_GPU_IO_RECEIVE));
		metricsMap.put(HDF_TRANSMITTED_BYTES, IMappingKey.of(METRICS, YAML_GPU_IO_TRANSMIT));
		metricsMap.put(HDF_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_GPU_POWER));
		metricsMap.put(HDF_RECEIVED_BYTES_RATE,
				IMappingKey.of(METRICS, YAML_GPU_IO_RECEIVE, AbstractMappingConverter::buildFakeCounterFunction));
		metricsMap.put(HDF_TRANSMITTED_BYTES_RATES,
				IMappingKey.of(METRICS, YAML_GPU_IO_TRANSMIT, AbstractMappingConverter::buildFakeCounterFunction));

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
		JsonNode deviceId = existingAttributes.get(HDF_DEVICE_ID);
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
		final JsonNode size = existingAttributes.get(HDF_SIZE);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				buildNameValue(firstDisplayArgument, new JsonNode[] { vendor, model }, size)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the GPU name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendorAndModel       {@link JsonNode} array of vendor and model to be joined 
	 * @param size                 {@link JsonNode} representing the GPU memory size in MB
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(
		final JsonNode firstDisplayArgument,
		final JsonNode[] vendorAndModel,
		final JsonNode size
	) {

		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(vendorAndModel).allMatch(Objects::isNull) && size == null) {
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

		// Means vendor, model or size is not null
		if (!sprintfArgs.isEmpty() || size != null) {
			format.append(
				Stream.concat(
					sprintfArgs
						.stream()
						.map(v -> "%s"),
					Stream.of(size)
						.filter(Objects::nonNull)
						.map(v -> {
							sprintfArgs.add(String.format("mebiBytes2HumanFormat(%s)", v.asText()));
							return "%s"; // MiB to human format
						})
				)
				.collect(Collectors.joining(" - "," (",")"))
			);
		}

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3), $column(4)) 
		// append the result to our format variable in order to get something like
		// sprintf("%s (%s - %s - %s)", $column(1), $column(2), $column(3), mebiBytes2HumanFormat($column(4)))
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
		convertOneToOneMetrics(key, value, (ObjectNode) node);

		final ObjectNode mapping = (ObjectNode) node;
		final JsonNode metrics = mapping.get(METRICS);

		if (metrics != null) {

			final JsonNode energy = metrics.get(YAML_GPU_ENERGY);
			final JsonNode power = metrics.get(YAML_GPU_POWER);

			if (power == null && energy != null && !energy.asText().contains("rate")) {
				((ObjectNode) metrics).set(
						YAML_GPU_POWER,
						new TextNode(
								buildRateFunction(
										getFunctionArgument(
												energy.asText()))));
			}

			if (energy == null && power != null && !power.asText().contains("fakeCounter")) {
				((ObjectNode) metrics).set(
						YAML_GPU_ENERGY,
						new TextNode(
								buildFakeCounterFunction(
										getFunctionArgument(
												power.asText()))));
			}
		}
	}

	@Override
	protected String getFunctionArgument(String value) {
		// It is not required to concatenated the value with the opening and closing quotation marks 
		if (value.indexOf("mebiBytes2HumanFormat") != -1) {
			return value;
		}
		return super.getFunctionArgument(value);
	}
}
