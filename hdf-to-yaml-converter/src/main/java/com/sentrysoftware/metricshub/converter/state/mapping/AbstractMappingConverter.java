package com.sentrysoftware.metricshub.converter.state.mapping;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.BOOLEAN_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.COMPUTE_POWER_SHARE_RATIO_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.CONDITIONAL_COLLECTION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.FAKE_COUNTER_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.FULL_DUPLEX_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_INTRUSION_STATUS_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_LED_STATUS_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_NEEDS_CLEANING_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_POWER_SUPPLY_UTILIZATION_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LEGACY_PREDICTED_FAILURE_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.LINK_STATUS_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.MEBI_BYTE_2_BYTE_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.MEGA_BIT_2_BIT_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.MEGA_HERTZ_2_HERTZ_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.METRICS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.PERCENT_2_RATIO_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.RATE_FORMAT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_DISK_CONTROLLER;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_ENCLOSURE;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_HW_PARENT_ID;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.YAML_HW_PARENT_TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.metricshub.converter.state.ConversionHelper;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractMappingConverter implements IMappingConverter {

	protected static final Pattern COLUMN_PATTERN = Pattern.compile("^\\$\\d+$");

	private static final Pattern PARAMETER_ACTIVATION_PATTERN = Pattern.compile(
		"(parameteractivation\\.([a-z0-9]+|[a-z0-9]+))\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * Get one to one attributes mapping
	 *
	 * @return key-pair values as {@link Map}
	 */
	protected abstract Map<String, Entry<String, IMappingKey>> getOneToOneAttributesMapping();

	/**
	 * Apply specific conversions of the given mapping attributes
	 *
	 * @param mapping            The mapping object node defining the <em>attributes</em> section
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes       Hardware Connector job's mapping existing temporary attributes
	 */
	protected abstract void convertAttributesSpecific(
		JsonNode mapping,
		ObjectNode existingAttributes,
		ObjectNode newAttributes
	);

	/**
	 * Set the name property in <code>newAttributes</code>
	 *
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes       Hardware Connector job's mapping existing temporary attributes
	 */
	protected abstract void setName(ObjectNode existingAttributes, ObjectNode newAttributes);

	/**
	 * Get one to one metrics mapping
	 *
	 * @return key-pair values as {@link Map}
	 */
	protected abstract Map<String, Entry<String, IMappingKey>> getOneToOneMetricsMapping();

	/**
	 * Convert the attributes section based on the one to one attribute mapping
	 * implemented in the concrete converter
	 *
	 * @param mapping Mapping node used to create metrics
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes Hardware Connector job's mapping existing temporary attributes
	 */
	private void convertOneToOneAttributes(
		final ObjectNode mapping,
		final ObjectNode existingAttributes,
		final ObjectNode newAttributes
	) {
		final Iterator<Entry<String, JsonNode>> iter = existingAttributes.fields();

		while (iter.hasNext()) {
			final Entry<String, JsonNode> attributeEntry = iter.next();

			final Entry<String, IMappingKey> mappingEntry = getOneToOneAttributesMapping().get(attributeEntry.getKey());
			if (mappingEntry != null) {
				final String where = mappingEntry.getKey();
				final IMappingKey mappingKey = mappingEntry.getValue();
				if (where.equals(ATTRIBUTES)) {
					convertKeyValueInNode(mappingKey, attributeEntry.getValue().asText(), newAttributes);
				} else if (where.equals(METRICS)) {
					final ObjectNode metrics = getOrCreateSubNode(METRICS, mapping);
					convertKeyValueInNode(mappingKey, attributeEntry.getValue().asText(), metrics);
				}
			}
		}
	}

	@Override
	public void postConvertDiscoveryProperties(final JsonNode mapping) {
		final ObjectNode existingAttributes = (ObjectNode) mapping.get(ATTRIBUTES);
		final ObjectNode newAttributes = JsonNodeFactory.instance.objectNode();

		convertOneToOneAttributes((ObjectNode) mapping, existingAttributes, newAttributes);

		convertAdditionalInformationToInfo(existingAttributes, newAttributes);

		convertParameterActivation((ObjectNode) mapping, existingAttributes);

		convertAttachmentProperties(existingAttributes, newAttributes);

		setName(existingAttributes, newAttributes);

		convertAttributesSpecific(mapping, existingAttributes, newAttributes);

		((ObjectNode) mapping).set(ATTRIBUTES, newAttributes);
	}

	/**
	 * Convert attachment properties. <br>
	 * <u>hw.parent.type</u><br>
	 * <ol>
	 *  <li>if ControllerNumber is provided: hw.parent.type=disk_controller</li>
	 *  <li>else if AttachedToDeviceType is provided: hw.parent.type=AttachedToDeviceType converted to YAML naming convention</li>
	 * </ol>
	 * <u>hw.parent.id</u><br>
	 * <ol>
	 *  <li>if ControllerNumber is provided: hw.parent.id=lookup("disk_controller", "id", "controller_number", $ControllerNumber)</li>
	 *  <li>else if AttachedToDeviceID is provided: hw.parent.id=AttachedToDeviceID value</li>
	 * </ol>
	 * @param existingAttributes
	 * @param newAttributes
	 */
	private void convertAttachmentProperties(final ObjectNode existingAttributes, final ObjectNode newAttributes) {
		final JsonNode controllerNumber = existingAttributes.get("controllernumber");
		final JsonNode attachedToDeviceId = existingAttributes.get("attachedtodeviceid");
		final JsonNode attachedToDeviceType = existingAttributes.get("attachedtodevicetype");
		if (controllerNumber != null && !(this instanceof DiskControllerConverter)) {
			newAttributes.set(YAML_HW_PARENT_TYPE, new TextNode(YAML_DISK_CONTROLLER));
			newAttributes.set(
				YAML_HW_PARENT_ID,
				new TextNode(
					String.format(
						"lookup(\"disk_controller\", \"id\", \"controller_number\", %s)",
						getFunctionArgument(controllerNumber.asText())
					)
				)
			);
			return;
		}

		if (!(this instanceof EnclosureConverter)) {
			if (attachedToDeviceType == null) {
				newAttributes.set(YAML_HW_PARENT_TYPE, new TextNode(YAML_ENCLOSURE));
			} else {
				String parentType = attachedToDeviceType.textValue();
				parentType = ConversionHelper.performValueConversions(parentType);
				if (parentType.equalsIgnoreCase("computer")) {
					parentType = YAML_ENCLOSURE;
				}
				newAttributes.set(YAML_HW_PARENT_TYPE, new TextNode(parentType));
			}

			if (attachedToDeviceId != null) {
				newAttributes.set(YAML_HW_PARENT_ID, new TextNode(attachedToDeviceId.asText()));
			}
		}
	}

	/**
	 * Convert additional information to the info attribute based on the given specification
	 * <ol>
	 *    <li>If all additional information are provided, build value: <em>join(" ", AdditionalInformation1, AdditionalInformation2, AdditionalInformation3)</em></li>
	 *    <li>If only additional information 1 and 2 are provided, build value: <em>join(" ", AdditionalInformation1, AdditionalInformation2)</em></li>
	 *    <li>If only the first additional information is provided, set the additional information value as is.</li>
	 * </ol>
	 *
	 * @param existingAttributes
	 * @param newAttributes
	 */
	private void convertAdditionalInformationToInfo(final ObjectNode existingAttributes, final ObjectNode newAttributes) {
		final JsonNode additionalInfo1 = existingAttributes.get("additionalinformation1");
		final JsonNode additionalInfo2 = existingAttributes.get("additionalinformation2");
		final JsonNode additionalInfo3 = existingAttributes.get("additionalinformation3");

		final List<JsonNode> infoList = Stream
			.of(additionalInfo1, additionalInfo2, additionalInfo3)
			.filter(Objects::nonNull)
			.toList();

		if (infoList.isEmpty()) {
			return;
		}

		final int size = infoList.size();
		final TextNode info;
		if (size >= 2) {
			final String function = infoList
				.stream()
				.map(JsonNode::asText)
				.map(this::getFunctionArgument)
				.collect(Collectors.joining(", ", "${awk::join(\" \", ", ")}"));

			info = new TextNode(function);
		} else {
			info = new TextNode(infoList.get(0).asText());
		}

		newAttributes.set("info", info);
	}

	/**
	 * The value is concatenated with the opening and closing quotation marks in
	 * case it doesn't match $\d+ pattern
	 *
	 * @param value to update
	 * @return String value
	 */
	protected String getFunctionArgument(final String value) {
		return COLUMN_PATTERN.matcher(value).matches() ? value : String.format("\"%s\"", value);
	}

	/**
	 * Get or create the sub node under the given mapping node
	 *
	 * @param subNodeKey The  key of the sub node
	 * @param mapping    The mapping node
	 * @return sub node as {@link ObjectNode}. Never <code>null</code>
	 */
	protected ObjectNode getOrCreateSubNode(final String subNodeKey, final ObjectNode mapping) {
		final JsonNode existingSubNode = mapping.get(subNodeKey);

		if (existingSubNode == null) {
			final ObjectNode node = JsonNodeFactory.instance.objectNode();
			mapping.set(subNodeKey, node);
			return node;
		}

		return (ObjectNode) existingSubNode;
	}

	/**
	 * Convert the given key-value pair using the one to one metrics mapping
	 * implemented in the concrete converter
	 *
	 * @param key     The HDF parameter key
	 * @param value   The value to set and converted to the YAML naming convention
	 * @param mapping The mapping {@link ObjectNode}
	 */
	protected void convertOneToOneMetrics(final String key, final String value, final ObjectNode mapping) {
		final Entry<String, IMappingKey> mappingEntry = getOneToOneMetricsMapping().get(key);
		if (mappingEntry != null) {
			final String where = mappingEntry.getKey();
			final IMappingKey mappingKey = mappingEntry.getValue();
			final ObjectNode node = getOrCreateSubNode(where, mapping);
			convertKeyValueInNode(mappingKey, ConversionHelper.performValueConversions(value), node);
		}
	}

	/**
	 * Convert the given mapping key-value in the given node
	 *
	 * @param mappingKey
	 * @param value
	 * @param node
	 */
	private void convertKeyValueInNode(final IMappingKey mappingKey, String value, final ObjectNode node) {
		if (mappingKey instanceof MappingKeyWithValueConverter mkwvc) {
			value = getFunctionArgument(value);
			node.set(mkwvc.getKey(), new TextNode(mkwvc.getValueConverter().apply(value)));
		} else if (mappingKey instanceof MappingKey mk) {
			node.set(mk.getKey(), new TextNode(value));
		} else {
			throw new IllegalArgumentException("Unrecognized IMappingKey");
		}
	}

	/**
	 * Convert ParameterActivation attributes to the conditionalCollection mapping based on the given specification
	 * <entity>.<job>.Instance.ParameterActivation.<metric>=<value>
	 * is to be converted to:
	 * monitors:
	 *   <entity>:
	 *     <job>:
	 *       mapping:
	 *         conditionalCollection:
	 *           <metric>: <value>
	 *
	 * @param mapping
	 * @param existingAttributes
	 */
	private void convertParameterActivation(final ObjectNode mapping, final ObjectNode existingAttributes) {
		final Iterator<Entry<String, JsonNode>> iter = existingAttributes.fields();

		while (iter.hasNext()) {
			final Entry<String, JsonNode> attributeEntry = iter.next();
			String key = attributeEntry.getKey();
			Matcher matcher = PARAMETER_ACTIVATION_PATTERN.matcher(key);

			if (matcher.matches()) {
				final ObjectNode conditionalCollection = getOrCreateSubNode(CONDITIONAL_COLLECTION, mapping);

				// We take the <metric> part of the key and try to convert it using the OneToOneAttributeMapping
				// or the metric itself if there is no match
				String metric = matcher.group(2);
				Entry<String, IMappingKey> mappingKey = getOneToOneMetricsMapping().get(metric);
				if (mappingKey != null) {
					conditionalCollection.set(mappingKey.getValue().getKey(), (TextNode) attributeEntry.getValue());
				} else {
					conditionalCollection.set(metric, (TextNode) attributeEntry.getValue());
				}
			}
		}
	}

	/**
	 * Build percent2Ratio(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildPercent2RatioFunction(final String value) {
		return String.format(PERCENT_2_RATIO_FORMAT, value);
	}

	/**
	 * Build megaHertz2Hertz(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildMegaHertz2HertzFunction(final String value) {
		return String.format(MEGA_HERTZ_2_HERTZ_FORMAT, value);
	}

	/**
	 * Build boolean(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildBooleanFunction(final String value) {
		return String.format(BOOLEAN_FORMAT, value);
	}

	/**
	 * Build fakeCounter(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildFakeCounterFunction(final String value) {
		return String.format(FAKE_COUNTER_FORMAT, value);
	}

	/**
	 * Build legacyIntrusionStatus(...) function
	 *
	 */
	public static String buildLegacyIntrusionStatusFunction(final String value) {
		return String.format(LEGACY_INTRUSION_STATUS_FORMAT, value);
	}

	/**
	 * Build rate(...) function
	 * @param value
	 * @return
	 */
	public static String buildRateFunction(final String value) {
		return String.format(RATE_FORMAT, value);
	}

	/**
	 * Build mebiByte2Byte(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildMebiByte2ByteFunction(final String value) {
		return String.format(MEBI_BYTE_2_BYTE_FORMAT, value);
	}

	/**
	 * Build legacyPredictedFailure(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildLegacyPredictedFailureFunction(final String value) {
		return String.format(LEGACY_PREDICTED_FAILURE_FORMAT, value);
	}

	/**
	 * Build legacyPowerSupplyUtilization(...) function
	 *
	 * @param value
	 * @return String value
	 */

	public static String buildLegacyPowerSupplyUtilizationFunction(final String value) {
		return String.format(LEGACY_POWER_SUPPLY_UTILIZATION_FORMAT, value);
	}

	/**
	 * Build legacyNeedsCleaning(...) function
	 * @param value
	 * @return String value
	 */
	public static String buildLegacyNeedsCleaningFunction(final String value) {
		return String.format(LEGACY_NEEDS_CLEANING_FORMAT, value);
	}

	/**
	 * Build computePowerShareRatio(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildComputePowerShareRatio(final String value) {
		return String.format(COMPUTE_POWER_SHARE_RATIO_FORMAT, value);
	}

	/**
	 * Build legacyLinkStatus(...) function
	 * @param value
	 * @return String value
	 */
	public static String buildLegacyLinkFunction(final String value) {
		return String.format(LINK_STATUS_FORMAT, value);
	}

	/**
	 * Build legacyFullDuplex(...) function
	 * @param value
	 * @return String value
	 */
	public static String buildLegacyFullDuplexFunction(final String value) {
		return String.format(FULL_DUPLEX_FORMAT, value);
	}

	/**
	 * Build megaBit2Bit(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildMegaBit2BitFunction(final String value) {
		return String.format(MEGA_BIT_2_BIT_FORMAT, value);
	}

	/**
	 * Build legacyLedStatus(...) function
	 *
	 * @param value
	 * @return String value
	 */
	public static String buildLegacyLedStatusFunction(final String value) {
		return String.format(LEGACY_LED_STATUS_FORMAT, value);
	}
}