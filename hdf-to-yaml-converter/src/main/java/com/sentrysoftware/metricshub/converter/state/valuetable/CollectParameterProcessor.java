package com.sentrysoftware.metricshub.converter.state.valuetable;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.COLLECT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.HDF_DEVICE_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_ID;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import com.sentrysoftware.metricshub.converter.state.mapping.IMappingConverter;
import com.sentrysoftware.metricshub.converter.state.mapping.MappingConvertersWrapper;

public class CollectParameterProcessor extends AbstractStateConverter {

	private static final Pattern COLLECT_PARAMETER_KEY_PATTERN = Pattern.compile(
		"^\\s*(([a-z]+)\\.collect\\.(?!(type|valuetable))([a-z]+))\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	@Override
	public boolean detect(String key, String value, JsonNode connector) {
		return value != null && key != null && getMatcher(key).matches();
	}

	@Override
	public void convert(String key, String value, JsonNode connector, PreConnector preConnector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final String monitorName = getMonitorName(matcher);

		final ObjectNode mapping = getOrCreateMapping(key, connector, COLLECT);

		final String property = matcher.group(4);

		if (property.equalsIgnoreCase(HDF_DEVICE_ID)) {
			JsonNode attributes = mapping.get(ATTRIBUTES);

			if (attributes == null) {
				attributes = JsonNodeFactory.instance.objectNode();
				mapping.set(ATTRIBUTES, attributes);
			}

			((ObjectNode) attributes).set(YAML_ID, new TextNode(ConversionHelper.performValueConversions(value)));

		} else {
			IMappingConverter m = new MappingConvertersWrapper().getConverter(monitorName);
			if (m != null) {
				m.convertCollectProperty(property, value, mapping);
			}
		}
	}

	@Override
	protected Matcher getMatcher(String key) {
		return COLLECT_PARAMETER_KEY_PATTERN.matcher(key);
	}
}
