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

public class EnclosureConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_VENDOR, IMappingKey.of(ATTRIBUTES, YAML_VENDOR));
		attributesMap.put(HDF_MODEL, IMappingKey.of(ATTRIBUTES, YAML_MODEL));
		attributesMap.put(HDF_SERIAL_NUMBER, IMappingKey.of(ATTRIBUTES, YAML_SERIAL_NUMBER));
		attributesMap.put(HDF_TYPE, IMappingKey.of(ATTRIBUTES, YAML_TYPE));
		attributesMap.put(HDF_DEVICE_HOSTNAME, IMappingKey.of(ATTRIBUTES, YAML_DEVICE_HOSTNAME));
		attributesMap.put(HDF_BIOS_VERSION, IMappingKey.of(ATTRIBUTES, YAML_BIOS_VERSION));
		attributesMap.put(HDF_FIRMWARE_VERSION, IMappingKey.of(ATTRIBUTES, YAML_BIOS_VERSION));

		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_ENCLOSURE_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_INTRUSION_STATUS, IMappingKey.of(METRICS, YAML_ENCLOSURE_INTRUSION_STATUS));
		metricsMap.put(HDF_ENERGY_USAGE, IMappingKey.of(METRICS, YAML_ENCLOSURE_ENERGY));
		metricsMap.put(HDF_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_ENCLOSURE_POWER));
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

		newAttributes.set(
				YAML_NAME,
				new TextNode(
						buildNameValue(firstDisplayArgument, new JsonNode[] { vendor, model }, type)));
	}

	/**
	 * Joins the given non-empty text nodes to build the battery name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendorAndModel       {@link JsonNode[]} array of vendor and model to
	 *                             be joined
	 * @param typeNode             {@link JsonNode} representing the type of the
	 *                             battery
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] vendorAndModel,
			final JsonNode typeNode) {

		final String firstArg = firstDisplayArgument.asText();
		if (typeNode == null && Stream.of(vendorAndModel).allMatch(Objects::isNull)) {
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
						.toList());

		// Means we have model or vendor but we don't know if have the type
		if (sprintfArgs.size() == 1) {
			format.append(" (%s");
		} else if (sprintfArgs.size() == 2) {
			// We have both model and vendor but we don't know if we have the type
			format.append(" (%s %s");
		}

		// Do we have the type?
		if (typeNode != null) {

			// Without vendor and model?
			if (sprintfArgs.isEmpty()) {
				// We append the type format only
				format.append(" (%s)");
			} else {
				// Append the type format
				format.append(" - %s)");
			}

			// Add the type to our list of arguments
			sprintfArgs.add(typeNode.asText());

		} else if (!sprintfArgs.isEmpty()) {
			// We have at least one of { vendor, model, type } let's close the parenthesis
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
								.collect(Collectors.joining(", ", "", ")")))
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
			final JsonNode intrusionStatus = metrics.get(YAML_ENCLOSURE_INTRUSION_STATUS);
			if (intrusionStatus != null && !intrusionStatus.asText().contains("legacyIntrusionStatus")) {
				((ObjectNode) metrics).set(
						YAML_ENCLOSURE_INTRUSION_STATUS,
						new TextNode(
								buildLegacyIntrusionStatusFunction(
										getFunctionArgument(
												intrusionStatus.asText()))));
			}

			final JsonNode energy = metrics.get(YAML_ENCLOSURE_ENERGY);
			final JsonNode power = metrics.get(YAML_ENCLOSURE_POWER);

			if (power == null && energy != null && !energy.asText().contains("rate")) {
				((ObjectNode) metrics).set(
						YAML_ENCLOSURE_POWER,
						new TextNode(
								buildRateFunction(
										getFunctionArgument(
												energy.asText()))));
			}

			if (energy == null && power != null && !power.asText().contains("fakeCounter")) {
				((ObjectNode) metrics).set(
						YAML_ENCLOSURE_ENERGY,
						new TextNode(
								buildFakeCounterFunction(
										getFunctionArgument(
												power.asText()))));
			}
		}
	}
}
