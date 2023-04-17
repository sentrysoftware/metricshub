package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CHEMISTRY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VENDOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CHEMISTRY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MODEL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TYPE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VENDOR;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class BatteryConverter extends AbstractMappingConverter {

	private static final Map<String, String> ONE_TO_ONE_ATTRIBUTES_MAPPING = Map.of(
		HDF_DEVICE_ID, YAML_ID,
		HDF_DISPLAY_ID, YAML_DISPLAY_ID,
		HDF_VENDOR, YAML_VENDOR,
		HDF_MODEL, YAML_MODEL,
		HDF_CHEMISTRY, YAML_CHEMISTRY,
		HDF_TYPE, YAML_TYPE
	);

	@Override
	public void convertCollectProperty(final String key, final String value, final JsonNode node) {
		// Implement
	}

	@Override
	protected Map<String, String> getOneToOneAttributesMapping() {
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
				buildNameValue(firstDisplayArgument, new JsonNode[] {vendor, model}, type)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the battery name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param vendorAndModel       {@link JsonNode[]} array of vendor and model to be joined 
	 * @param typeNode             {@link JsonNode} representing the type of the battery
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] vendorAndModel, final JsonNode typeNode) {

		final String firstArg = firstDisplayArgument.asText();
		if (typeNode  == null && Stream.of(vendorAndModel).allMatch(Objects::isNull)) {
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

		// Here we will have a string like
		// sprint("%s (%s %s - %s)", 
		format.append("\", ");

		// Add the first argument at the beginning of the list 
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $column(1), $column(2), $column(3)) 
		// append the result to our format variable in order to get something like
		// sprint("%s (%s %s - %s)", $column(1), $column(2), $column(3))
		return format
			.append(
				sprintfArgs
					.stream()
					.map(this::getFunctionArgument)
					.collect(Collectors.joining(", ", "", ")"))
			)
			.toString();

	}

}
