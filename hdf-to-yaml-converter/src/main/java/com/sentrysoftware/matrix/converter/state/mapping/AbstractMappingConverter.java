package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISK_CONTROLLER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ENCLOSURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_HW_PARENT_ID;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_HW_PARENT_TYPE;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.sentrysoftware.matrix.converter.state.ConversionHelper;

public abstract class AbstractMappingConverter implements IMappingConverter {

	protected static final Pattern COLUMN_PATTERN = Pattern.compile("^\\$column\\(\\d+\\)$");

	/**
	 * Get the one to one mapping attributes
	 * 
	 * @return key-pair values as {@link Map}
	 */
	protected abstract Map<String, String> getOneToOneAttributesMapping();

	/**
	 * Apply specific conversions of the given mapping attributes
	 * 
	 * @param mapping            The mapping object node defining the <em>attributes</em> section 
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes       Hardware Connector job's mapping existing temporary attributes
	 */
	protected abstract void convertAttributesSpecific(
			final JsonNode mapping,
			final ObjectNode existingAttributes,
			final ObjectNode newAttributes
	);

	/**
	 * Set the name property in <code>newAttributes</code>
	 * 
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes       Hardware Connector job's mapping existing temporary attributes
	 */
	protected abstract void setName(ObjectNode existingAttributes, ObjectNode newAttributes);

	/**
	 * Convert the attributes section based on the one to one attribute mapping
	 * implemented in the concrete converter
	 * 
	 * @param existingAttributes Hardware Connector job's mapping existing temporary attributes
	 * @param newttributes Hardware Connector job's mapping existing temporary attributes
	 */
	private void convertOneToOneAttributes(final ObjectNode existingAttributes, final ObjectNode newAttributes) {

		final Iterator<Entry<String, JsonNode>> iter = existingAttributes.fields();

		while (iter.hasNext()) {
			final Entry<String, JsonNode> attributeEntry = iter.next();

			final String newKey = getOneToOneAttributesMapping().get(attributeEntry.getKey());
			if (newKey != null) {
				newAttributes.set(newKey, attributeEntry.getValue());
			}
		}
	}

	@Override
	public void postConvertDiscoveryProperties(final JsonNode mapping) {
		final ObjectNode  existingAttributes = (ObjectNode) mapping.get(ATTRIBUTES);
		final ObjectNode newAttributes = JsonNodeFactory.instance.objectNode();

		convertOneToOneAttributes(existingAttributes, newAttributes);

		convertAdditionalInformationToInfo(existingAttributes, newAttributes);

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
		if (controllerNumber != null) {
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

		if (attachedToDeviceType == null) {
			newAttributes.set(YAML_HW_PARENT_TYPE, new TextNode(YAML_ENCLOSURE));
		} else {
			String parentType = attachedToDeviceType.textValue();
			parentType = ConversionHelper.HDF_TO_YAML_MONITOR_NAME.getOrDefault(parentType, parentType);
			if (parentType.equalsIgnoreCase("computer")) {
				parentType = YAML_ENCLOSURE;
			}
			newAttributes.set(YAML_HW_PARENT_TYPE, new TextNode(parentType));
		}

		if (attachedToDeviceId != null) {
			newAttributes.set(YAML_HW_PARENT_ID, new TextNode(attachedToDeviceId.asText()));
		}
	}

	/**
	 * Convert additional information to the info attribute based on the given specification
	 * <ol>
	 *    <li>If all additional information are provided, build value: <em>join(AdditionalInformation1, AdditionalInformation2, AdditionalInformation3, " ")</em></li>
	 *    <li>If only additional information 1 and 2 are provided, build value: <em>join(AdditionalInformation1, AdditionalInformation2, " ")</em></li>
	 *    <li>If only the first additional information is provided, set the additional information value as is.</li>
	 * </ol>
	 * 
	 * @param existingAttributes
	 * @param newAttributes
	 */
	private void convertAdditionalInformationToInfo(
		final ObjectNode existingAttributes,
		final ObjectNode newAttributes
	) {

		final JsonNode additionalInfo1 = existingAttributes.get("additionalinformation1");
		final JsonNode additionalInfo2 = existingAttributes.get("additionalinformation2");
		final JsonNode additionalInfo3 = existingAttributes.get("additionalinformation3");

		final List<JsonNode> infoList = Stream.of(
				additionalInfo1,
				additionalInfo2,
				additionalInfo3
			)
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
				.collect(Collectors.joining(", ", "join(", ", \" \")"));

			info = new TextNode(function); 
		} else {
			info = new TextNode(infoList.get(0).asText());
		}

		newAttributes.set("info", info);
	}

	/**
	 * The value is concatenated with the opening and closing quotation marks in
	 * case it doesn't match $column(\d+) pattern
	 * 
	 * @param value to update
	 * @return String value
	 */
	protected String getFunctionArgument(final String value) {
		return COLUMN_PATTERN.matcher(value).matches() ? value : String.format("\"%s\"", value);
	}

}
