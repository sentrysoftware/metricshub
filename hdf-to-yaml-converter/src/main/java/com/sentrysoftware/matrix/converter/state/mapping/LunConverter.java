package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ARRAY_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_AVAILABLE_PATH_COUNT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_AVAILABLE_PATH_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LOCAL_DEVICE_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_REMOTE_DEVICE_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_WWN;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ARRAY_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_AVAILABLE_PATH_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LOCAL_DEVICE_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LUN_PATHS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LUN_PATHS_LIMIT_LOW_DEGRADED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LUN_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_REMOTE_DEVICE_NAME;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LunConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_LOCAL_DEVICE_NAME, IMappingKey.of(ATTRIBUTES, YAML_LOCAL_DEVICE_NAME));
		attributesMap.put(HDF_REMOTE_DEVICE_NAME, IMappingKey.of(ATTRIBUTES, YAML_REMOTE_DEVICE_NAME));
		attributesMap.put(HDF_ARRAY_NAME, IMappingKey.of(ATTRIBUTES, YAML_ARRAY_NAME));
		attributesMap.put(HDF_WWN, IMappingKey.of(ATTRIBUTES, HDF_WWN));
		attributesMap.put(HDF_AVAILABLE_PATH_WARNING, IMappingKey.of(METRICS, YAML_LUN_PATHS_LIMIT_LOW_DEGRADED));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_LUN_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(
			HDF_AVAILABLE_PATH_INFORMATION,
			IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_AVAILABLE_PATH_INFORMATION)
		);
		metricsMap.put(HDF_AVAILABLE_PATH_COUNT, IMappingKey.of(METRICS, YAML_LUN_PATHS));

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

		final JsonNode displayId = existingAttributes.get(HDF_ARRAY_NAME);
		JsonNode firstDisplayArgument = deviceId;
		if (displayId != null) {
			firstDisplayArgument = displayId;
		}

		final JsonNode local = existingAttributes.get(HDF_LOCAL_DEVICE_NAME);
		final JsonNode remote = existingAttributes.get(HDF_REMOTE_DEVICE_NAME);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				wrapInAwkRefIfFunctionDetected(buildNameValue(firstDisplayArgument, new JsonNode[] { local, remote }))
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the LUN name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param localAndRemote       {@link JsonNode} array of local and remote to be joined
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode[] localAndRemote) {
		final String firstArg = firstDisplayArgument.asText();
		if (Stream.of(localAndRemote).allMatch(Objects::isNull)) {
			return firstArg;
		}

		// Create the function with the first format for the first argument
		final StringBuilder format = new StringBuilder("sprintf(\"%s");

		// Build the list of arguments non-null
		final List<String> sprintfArgs = new ArrayList<>();
		sprintfArgs.addAll(Stream.of(localAndRemote).filter(Objects::nonNull).map(JsonNode::asText).toList());

		// Means local or remote is not empty
		if (!sprintfArgs.isEmpty()) {
			format.append(sprintfArgs.stream().map(v -> "%s").collect(Collectors.joining(" - ", " (", ")")));
		}

		// Add the first argument at the beginning of the list
		sprintfArgs.add(0, firstArg);

		// Join the arguments: $1, $2, $3)
		// append the result to our format variable in order to get something like
		// sprintf("%s (%s - %s)", $1, $2, $3)
		return format
			.append("\", ") // Here we will have a string like sprintf("%s (%s - %s)",
			.append(sprintfArgs.stream().map(this::getFunctionArgument).collect(Collectors.joining(", ", "", ")")))
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
