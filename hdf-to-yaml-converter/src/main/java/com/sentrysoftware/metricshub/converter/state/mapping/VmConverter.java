package com.sentrysoftware.metricshub.converter.state.mapping;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_HOSTNAME;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_POWER_CONSUMPTION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_VM_POWER_RATIO;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_VM_POWER_STATE;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VM_ENERGY;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VM_HOSTNAME;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VM_POWER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VM_POWER_RATIO;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_VM_POWER_STATE;
import static com.sentrysoftware.metricshub.converter.state.ConversionHelper.wrapInAwkRefIfFunctionDetected;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class VmConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_HOSTNAME, IMappingKey.of(ATTRIBUTES, YAML_VM_HOSTNAME));
		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;

	static {
		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_VM_POWER_STATE, IMappingKey.of(METRICS, YAML_VM_POWER_STATE));
		metricsMap.put(
			HDF_VM_POWER_RATIO,
			IMappingKey.of(METRICS, YAML_VM_POWER_RATIO, AbstractMappingConverter::buildComputePowerShareRatio)
		);
		metricsMap.put(HDF_POWER_CONSUMPTION, IMappingKey.of(METRICS, YAML_VM_POWER));
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
		JsonNode deviceId = existingAttributes.get(HDF_DEVICE_ID);
		if (deviceId == null) {
			return;
		}

		final JsonNode displayId = existingAttributes.get(HDF_DISPLAY_ID);

		newAttributes.set(
			YAML_NAME,
			new TextNode(
				wrapInAwkRefIfFunctionDetected(
					buildNameValue(displayId != null ? displayId : deviceId, existingAttributes.get(HDF_HOSTNAME))
				)
			)
		);
	}

	/**
	 * Joins the given non-empty text nodes to build the VM name value
	 *
	 * @param firstDisplayArgument {@link JsonNode} representing the display name
	 * @param hostnameNode         {@link JsonNode} hostname node
	 *
	 * @return {@link String} Joined text nodes
	 */
	private String buildNameValue(final JsonNode firstDisplayArgument, final JsonNode hostnameNode) {
		final String firstArg = firstDisplayArgument.asText();
		if (hostnameNode == null) {
			return firstArg;
		}

		return new StringBuilder("sprintf(\"%s (%s)\", ")
			.append(getFunctionArgument(firstArg))
			.append(", ")
			.append(getFunctionArgument(hostnameNode.asText()))
			.append(")")
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
			final JsonNode powerConsumption = metrics.get(YAML_VM_POWER);
			if (powerConsumption != null) {
				((ObjectNode) metrics).set(
						YAML_VM_ENERGY,
						new TextNode(buildFakeCounterFunction(getFunctionArgument(powerConsumption.asText())))
					);
			}
		}
	}
}
