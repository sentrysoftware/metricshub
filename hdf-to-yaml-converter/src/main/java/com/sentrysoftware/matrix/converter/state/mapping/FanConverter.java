package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PERCENT_ALARM_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PERCENT_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_SPEED_PERCENT;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_STATUS_INFORMATION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_WARNING_THRESHOLD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.LEGACY_TEXT_PARAMETERS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISPLAY_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED_LIMIT_CRITICAL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED_LIMIT_DEGRADED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED_RATIO;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED_RATIO_LIMIT_CRITICAL;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_SPEED_RATIO_LIMIT_DEGRADED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN_STATUS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NAME;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_STATUS_INFORMATION;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class FanConverter extends AbstractMappingConverter {

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_ATTRIBUTES_MAPPING;
	static {
		final Map<String, Entry<String, IMappingKey>> attributesMap = new HashMap<>();
		attributesMap.put(HDF_DEVICE_ID, IMappingKey.of(ATTRIBUTES, YAML_ID));
		attributesMap.put(HDF_DISPLAY_ID, IMappingKey.of(ATTRIBUTES, YAML_DISPLAY_ID));
		attributesMap.put(HDF_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_FAN_SPEED_LIMIT_DEGRADED));
		attributesMap.put(HDF_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_FAN_SPEED_LIMIT_CRITICAL));
		attributesMap.put(HDF_PERCENT_WARNING_THRESHOLD, IMappingKey.of(METRICS, YAML_FAN_SPEED_RATIO_LIMIT_DEGRADED, AbstractMappingConverter::buildPercent2RatioFunction));
		attributesMap.put(HDF_PERCENT_ALARM_THRESHOLD, IMappingKey.of(METRICS, YAML_FAN_SPEED_RATIO_LIMIT_CRITICAL, AbstractMappingConverter::buildPercent2RatioFunction));

		ONE_TO_ONE_ATTRIBUTES_MAPPING = Collections.unmodifiableMap(attributesMap);
	}

	private static final Map<String, Entry<String, IMappingKey>> ONE_TO_ONE_METRICS_MAPPING;
	static {

		final Map<String, Entry<String, IMappingKey>> metricsMap = new HashMap<>();
		metricsMap.put(HDF_STATUS, IMappingKey.of(METRICS, YAML_FAN_STATUS));
		metricsMap.put(HDF_STATUS_INFORMATION, IMappingKey.of(LEGACY_TEXT_PARAMETERS, YAML_STATUS_INFORMATION));
		metricsMap.put(HDF_SPEED, IMappingKey.of(METRICS, YAML_FAN_SPEED));
		metricsMap.put(HDF_SPEED_PERCENT, IMappingKey.of(METRICS, YAML_FAN_SPEED_RATIO, AbstractMappingConverter::buildPercent2RatioFunction));
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
			throw new IllegalStateException(String.format("%s cannot be null.", HDF_DEVICE_ID));
		}

		final JsonNode displayId = existingAttributes.get(HDF_DISPLAY_ID);

		newAttributes.set(
				YAML_NAME,
				new TextNode((displayId != null ? displayId : deviceId).asText())
				);
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