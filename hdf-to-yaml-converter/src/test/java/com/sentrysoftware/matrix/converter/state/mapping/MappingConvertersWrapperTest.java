package com.sentrysoftware.matrix.converter.state.mapping;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.converter.ConverterConstants;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

class MappingConvertersWrapperTest {

	private static final String YAML_ATTR_1 = "attr_1";
	private static final String HDF_ATTR_1 = "attr1";
	private static final String YAML_ATTR_2 = "attr_2";
	private static final String HDF_ATTR_2 = "attr2";
	private static final String COLUMN_1 = "$column(1)";
	private static final String COLUMN_2 = "$column(2)";
	private static final String HDF_STATUS = "status";
	private static final String YAML_STATUS = "hw.status{hw.type=\"enclosure\"}";

	private static final Map<String, IMappingConverter> CONVERTERS = Map.of(
		ConversionHelper.YAML_ENCLOSURE, new TestMappingConverter()
	);

	private static final MappingConvertersWrapper MAPPING_CONVERTERS_WRAPPER  = new MappingConvertersWrapper(CONVERTERS);

	@Test
	@Disabled("Until MappingConvertersWrapper is up")
	void testConvertParameterActivation() {
		final JsonNode conditionalCollection = JsonNodeFactory.instance.objectNode();
		MAPPING_CONVERTERS_WRAPPER.convertParameterActivation("parameteractivation.status", COLUMN_1, ConversionHelper.YAML_ENCLOSURE, conditionalCollection);
		final ObjectNode expected = JsonNodeFactory.instance.objectNode();
		expected.set(YAML_STATUS, new TextNode(COLUMN_1));
		assertEquals(expected, conditionalCollection);
	}

	@Test
	@Disabled("Until MappingConvertersWrapper is up")
	void testGetConverterForMonitorType() {
		assertNotNull(MAPPING_CONVERTERS_WRAPPER.getConverterForMonitorType(ConversionHelper.YAML_ENCLOSURE));
	}

	@Test
	@Disabled("Until MappingConvertersWrapper is up")
	void testConvertCollectProperty() {
		final JsonNode metrics = JsonNodeFactory.instance.objectNode();
		MAPPING_CONVERTERS_WRAPPER.convertParameterActivation(HDF_STATUS, COLUMN_1, ConversionHelper.YAML_ENCLOSURE, metrics);
		final ObjectNode expected = JsonNodeFactory.instance.objectNode();
		expected.set(YAML_STATUS, new TextNode(COLUMN_1));
		assertEquals(expected, metrics);
	}

	@Test
	@Disabled("Until MappingConvertersWrapper is up")
	void testPostConvertDiscovery() {
		final ObjectNode connector = JsonNodeFactory.instance.objectNode();
		final ObjectNode monitors = JsonNodeFactory.instance.objectNode();
		final ObjectNode discovery = JsonNodeFactory.instance.objectNode();
		final ObjectNode enclosure = JsonNodeFactory.instance.objectNode();
		final ObjectNode mapping = JsonNodeFactory.instance.objectNode();
		final ObjectNode attributes = JsonNodeFactory.instance.objectNode();
		connector.set(ConverterConstants.MONITORS, monitors);
		monitors.set(ConversionHelper.YAML_ENCLOSURE, enclosure);
		enclosure.set(ConverterConstants.DISCOVERY, discovery);
		discovery.set(ConverterConstants.MAPPING, mapping);
		mapping.set(ConverterConstants.ATTRIBUTES, attributes);
		attributes.set(HDF_ATTR_1, new TextNode(COLUMN_1));
		attributes.set(HDF_ATTR_2, new TextNode(COLUMN_2));

		MAPPING_CONVERTERS_WRAPPER.postConvertDiscovery(connector);
		final ObjectNode expected = JsonNodeFactory.instance.objectNode();
		expected.set(YAML_ATTR_1, new TextNode(COLUMN_1));
		expected.set(YAML_ATTR_2, new TextNode(COLUMN_2));
		assertEquals(expected, mapping.get(ConverterConstants.ATTRIBUTES));
	}

	static class TestMappingConverter implements IMappingConverter {

		static final Map<String, String> ATTRIBUTE_MAPPING = Map.of(HDF_ATTR_1, YAML_ATTR_1, HDF_ATTR_2, YAML_ATTR_2);
		static final Map<String, String> METRICS_MAPPING = Map.of(HDF_STATUS, YAML_STATUS);

		@Override
		public void postConvertDiscoveryProperties(JsonNode mapping) {
			final ObjectNode newAttributes = JsonNodeFactory.instance.objectNode();
			final JsonNode attributes = mapping.get(ConverterConstants.ATTRIBUTES);
			final Iterator<Entry<String, JsonNode>> iter = attributes.fields();
			while (iter.hasNext()) {
				final Entry<String, JsonNode> node = iter.next();
				final String newKey = ATTRIBUTE_MAPPING.get(node.getKey());
				assertNotNull(newKey);
				newAttributes.set(newKey, node.getValue());
			}
			((ObjectNode) mapping).set(ConverterConstants.ATTRIBUTES, newAttributes);
		}

		@Override
		public void convertCollectProperty(String key, String value, JsonNode node) {
			final String newKey = METRICS_MAPPING.get(key);
			assertNotNull(newKey);
			((ObjectNode) node)
				.set(
					newKey,
					JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value))
				);
		}
	}
}
