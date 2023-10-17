package com.sentrysoftware.metricshub.converter.state;

import static com.sentrysoftware.metricshub.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.COMPUTES;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.CONNECTOR;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.CRITERIA;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DETECTION;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DETECTION_DOT_CRITERIA;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DISCOVERY;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DOT;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.DOT_COMPUTE;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.MAPPING;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.MONITORS;
import static com.sentrysoftware.metricshub.converter.ConverterConstants.SOURCES;
import static com.sentrysoftware.metricshub.converter.state.ConversionHelper.performValueConversions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.metricshub.converter.PreConnector;
import com.sentrysoftware.metricshub.converter.state.detection.snmp.OidProcessor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.NonNull;

public abstract class AbstractStateConverter implements IConnectorStateConverter {

	protected static final String INVALID_KEY_MESSAGE_FORMAT = "Invalid key: %s";

	protected abstract Matcher getMatcher(String key);

	@Override
	public boolean detect(final String key, final String value, final JsonNode connector) {
		if (value == null || key == null) {
			return false;
		}

		final Matcher matcher = getMatcher(key);

		if (!matcher.matches()) {
			return false;
		}

		return isAccurateContext(key, value, matcher, connector);
	}

	/**
	 * @param key       The key will determine which context evaluation method should be called:<br>
	 *                  <ul>
	 *                  	<li>
	 *                      	if the key starts with "detection.criteria" (ignoring case),
	 *                      	the criterion context evaluation method will be called.
	 *                  	</li>
	 *                  	<li>
	 *                      	if the key contains ".compute" (ignoring case),
	 *                      	the compute context evaluation method will be called.
	 *                  	</li>
	 *                  	<li>otherwise, the source context evaluation will be called.</li>
	 *                  </ul>
	 * @param value     The value used to determine the context.
	 * @param matcher   The {@link Matcher} used to determine the context of the source or the compute.
	 * @param connector	The {@link JsonNode} used to determine the context.
	 *
	 * @return			Whether or not the context evaluation method call evaluates to <i>true</i>.
	 */
	private boolean isAccurateContext(
		final String key,
		final String value,
		final Matcher matcher,
		final JsonNode connector
	) {
		if (key.startsWith(DETECTION_DOT_CRITERIA)) {
			return isCriterionContext(value, connector);
		}

		return key.contains(DOT_COMPUTE)
			? isComputeContext(value, matcher, connector)
			: isSourceContext(value, matcher, connector);
	}

	/**
	 * @param value		The value of the source type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param matcher   The {@link Matcher} used to determine the context of the source.
	 * @param connector	The connector used to retrieve the source
	 * 					in case <i>this</i> is not a <i>TypeProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose type matches the given value.</li>
	 * 								<li>
	 * 									<i>this</i> is not a <i>TypeProcessor</i>
	 * 									and there is a source in the given connector
	 * 									matching the given {@link Matcher}.
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isSourceContext(final String value, final Matcher matcher, final JsonNode connector) {
		if (this instanceof com.sentrysoftware.metricshub.converter.state.source.common.TypeProcessor typeProcessor) {
			return typeProcessor.getHdfType().equalsIgnoreCase(value);
		}

		return getSource(matcher, connector) != null;
	}

	/**
	 * @param matcher			A <u>NON-NULL</u> {@link Matcher},
	 * 							whose match operation has been called successfully,
	 * 							and from which
	 * 							a monitor name, a job name
	 * 							and a source index can be extracted.
	 * @param connector			The connector whose compute is being searched for.
	 *
	 * @return					The {@link ObjectNode} representing the compute in the given {@link JsonNode}
	 * 							connector matching the given {@link Matcher}, if available.
	 */
	protected ObjectNode getLastCompute(final Matcher matcher, final JsonNode connector) {
		final ObjectNode source = getSource(matcher, connector);
		if (source == null) {
			return null;
		}

		final ArrayNode computes = (ArrayNode) source.get(COMPUTES);
		if (computes == null || computes.isEmpty()) {
			return null;
		}

		return (ObjectNode) computes.get(computes.size() - 1);
	}

	/**
	 * @param matcher			A <u>NON-NULL</u> {@link Matcher},
	 * 							whose match operation has been called successfully,
	 * 							and from which
	 * 							a monitor name, a job name
	 * 							and a source index can be extracted.
	 * @param connector			The connector whose source is being searched for.
	 *
	 * @return					The {@link ObjectNode} representing the source in the given {@link JsonNode}
	 * 							connector matching the given {@link Matcher}, if available.
	 */
	protected ObjectNode getSource(final Matcher matcher, final JsonNode connector) {
		final String monitorName = getMonitorName(matcher);
		final String monitorJobName = getMonitorJobName(matcher);
		final String sourceName = getSourceName(matcher);

		final ObjectNode monitors = (ObjectNode) connector.get(MONITORS);
		if (monitors == null) {
			return null;
		}

		final ObjectNode monitor = (ObjectNode) monitors.get(monitorName);
		if (monitor == null) {
			return null;
		}

		final ObjectNode job = (ObjectNode) monitor.get(monitorJobName);
		if (job == null) {
			return null;
		}

		final ObjectNode sources = (ObjectNode) job.get(SOURCES);
		if (sources == null) {
			return null;
		}

		return (ObjectNode) sources.get(sourceName);
	}

	/**
	 * Extracts the name of the hardware monitor's job
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the job is expected to be the third capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.<b><u>(discovery|collect)</u></b>\\.source\\(([1-9]\\d*)\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.<b><u>discovery</u></b>.source(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>discovery</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the hardware monitor's job,
	 * 					which is supposed to be the third capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws IllegalArgumentException		If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no third capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 */
	protected String getMonitorJobName(@NonNull Matcher matcher) {
		return matcher.group(3).toLowerCase();
	}

	/**
	 * Extracts the name of the hardware monitor
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The name of the hardware monitor is expected to be the second capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*(<b><u>(.*)</u></b>\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i><b><u>enclosure</u></b>.discovery.source(1).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>enclosure</u></b></i>.
	 *
	 * @param matcher	A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The name of the {@link HardwareMonitor},
	 * 					which is supposed to be the second capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws IllegalArgumentException		If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no second capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 */
	protected String getMonitorName(@NonNull Matcher matcher) {
		return ConversionHelper.getYamlMonitorName(matcher.group(2));
	}

	/**
	 * Extracts the index of the source
	 * from the {@link String} against which the given {@link Matcher}'s inner {@link Pattern} has been tested.<br>
	 * The index of the source is expected to be the fourth capturing group during the last match operation<br><br>
	 *
	 * <b><u>Note</u></b>: you should <u>NOT</u> call this method
	 * 					   before making sure <i>matcher.matches()</i> or <i>matcher.find()</i> has been called
	 * 					   and returned <i>true</i>.<br><br>
	 *
	 * <b><u>Example</u></b>: Assuming the inner {@link Pattern}'s regex is:<br>
	 * 						  <i>^\\s*((.*)\\.(discovery|collect)\\.source\\(<b><u>([1-9]\\d*)</u></b>\\))\\.type\\s*$</i>
	 * 						  <br>and the {@link String} against which the {@link Pattern} has been tested is:<br>
	 * 						  <i>enclosure.discovery.source(<b><u>1</u></b>).type</i><br>
	 * 						  which does match,
	 * 						  the returned value would be <i><b><u>source(1)</u></b></i>.
	 *
	 * @param matcher   A <u>NON-NULL</u> {@link Matcher}
	 *                  whose <i>matches()</i> or <i>find()</i> method has already been called and returned <i>true</i>.
	 *
	 * @return			The index of the source,
	 * 					which is supposed to be the fourth capturing group of the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 * @throws IllegalArgumentException		If the given {@link Matcher} is null.
	 *
	 * @throws IllegalStateException		If no match has yet been attempted,
	 * 										or if the previous match operation failed.
	 *
	 * @throws IndexOutOfBoundsException	If there is no fourth capturing group
	 * 										in the given {@link Matcher}'s inner {@link Pattern}.
	 *
	 */
	protected String getSourceName(@NonNull Matcher matcher) {
		return String.format("source(%s)", matcher.group(4));
	}

	/**
	 * @param value		The value of the compute type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param matcher   The {@link Matcher} used to determine the compute context
	 * @param connector	The {@link @JsonNode} used to retrieve the compute
	 * 					in case <i>this</i> is not a <i>TypeProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose type matches the given value.</li>
	 * 								<li>
	 * 									<i>this</i> is not a <i>TypeProcessor</i>
	 * 									and there is a current compute in the given {@link @JsonNode} under the computes section
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isComputeContext(final String value, final Matcher matcher, final JsonNode connector) {
		if (this instanceof com.sentrysoftware.metricshub.converter.state.computes.common.ComputeTypeProcessor typeProcessor) {
			return typeProcessor.getHdfType().equalsIgnoreCase(value);
		}

		return getLastCompute(matcher, connector) != null;
	}

	/**
	 *
	 * @param value		The value of the criterion type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param connector The {@link JsonNode} used to retrieve the criterion
	 * 					in case <i>this</i> is not an <i>OidProcessor</i>
	 *
	 * @return			<ul>
	 * 						<li>
	 * 							<b>true</b> if:
	 * 							<ul>
	 * 								<li><i>this</i> is a <i>TypeProcessor</i> whose HDF type matches the given value.</li>
	 * 								<li><i>this</i> is an <i>OidProcessor</i> that manages <i>SNMPGet</i> and <i>SNMPGetNext</i> creation.</li>
	 * 								<li>
	 * 									<i>this</i> is neither a <i>TypeProcessor</i> nor an <i>OidProcessor</i>
	 * 									and there is a last criterion in the criteria section of the given connector
	 *									{@link JsonNode}
	 * 								</li>
	 * 							</ul>
	 * 						</li>
	 * 						<li><b>false</b> otherwise.</li>
	 *					</ul>
	 */
	private boolean isCriterionContext(final String value, final JsonNode connector) {
		if (this instanceof com.sentrysoftware.metricshub.converter.state.detection.common.TypeProcessor typeProcessor) {
			return typeProcessor.getHdfType().equalsIgnoreCase(value);
		}

		return (this instanceof OidProcessor) || getLastCriterion(connector) != null;
	}

	/**
	 * Get last criterion defined under the <i>connector: detection: criteria</i> path.
	 * @param connector	The {@link JsonNode} whose criterion is being searched for.
	 *
	 * @return	The criterion in the given {@link JsonNode}
	 */
	protected JsonNode getLastCriterion(final @NonNull JsonNode connector) {
		final JsonNode connectorSection = connector.get(CONNECTOR);

		if (connectorSection == null) {
			return null;
		}

		final JsonNode detection = connectorSection.get(DETECTION);
		if (detection == null) {
			return null;
		}

		final ArrayNode criteria = (ArrayNode) detection.get(CRITERIA);
		if (criteria == null || criteria.isEmpty()) {
			return null;
		}

		return criteria.get(criteria.size() - 1);
	}

	/**
	 * @param key		The key used to check the criterion context
	 * @param connector	The {@link JsonNode} whose criterion is being searched for.
	 *
	 * @return	The criterion matching the given key.
	 */
	protected JsonNode getLastCriterion(final String key, final JsonNode connector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final JsonNode criterion = getLastCriterion(connector);

		if (criterion == null) {
			throw new IllegalStateException("Could not find any Criterion for the following key: " + key + DOT);
		}

		return criterion;
	}

	/**
	 * Create a new text node in the last criterion object
	 *
	 * @param key The key of the criterion context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createCriterionTextNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = (ObjectNode) getLastCriterion(key, connector);
		createTextNode(newNodeKey, value, objectNode);
	}

	/**
	 * Create a new boolean node in the last criterion object
	 *
	 * @param key The key of the criterion context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createCriterionBooleanNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = (ObjectNode) getLastCriterion(key, connector);
		createBooleanNode(newNodeKey, value, objectNode);
	}

	/**
	 * Create a new integer node in the last criterion object
	 *
	 * @param key The key of the criterion context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createCriterionIntegerNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = (ObjectNode) getLastCriterion(key, connector);
		createIntegerNode(newNodeKey, value, objectNode);
	}

	/**
	 * Create a new array node in the last criterion object
	 *
	 * @param key The key of the criterion context
	 * @param arrayValues The array values to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createCriterionStringArrayNode(
		final String key,
		final String[] arrayValues,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = (ObjectNode) getLastCriterion(key, connector);
		createStringArrayNode(newNodeKey, arrayValues, objectNode);
	}

	/**
	 * Create a new array node in the last criterion object
	 *
	 * @param key The key of the criterion context
	 * @param arrayValues The array values to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createSourceStringArrayNode(
		final String key,
		final String[] arrayValues,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = getCurrentSource(key, connector);
		createStringArrayNode(newNodeKey, arrayValues, objectNode);
	}

	/**
	 * Create the a new array node with the array values in the given object node
	 *
	 * @param key The node key
	 * @param arrayValues The array values to add in the new array node
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createStringArrayNode(final String key, final String[] arrayValues, final ObjectNode objectNode) {
		final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		Stream.of(arrayValues).forEach(value -> arrayNode.add(performValueConversions(value)));
		objectNode.set(key, arrayNode);
	}

	/**
	 * Create the a new text node in the given object node
	 *
	 * @param key The node key
	 * @param value The text value
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createTextNode(final String key, final String value, final ObjectNode objectNode) {
		final String converted = performValueConversions(value);

		objectNode.set(key, JsonNodeFactory.instance.textNode(converted));
	}

	/**
	 * Create the a new boolean node in the given object node if the text value is either
	 * “1“, “0”, “true”, or “false“. Create a textNode using the text value in any other case.
	 *
	 * @param key The node key
	 * @param value The text value
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createBooleanNode(final String key, final String value, final ObjectNode objectNode) {
		final String trimedValue = value.trim();
		if (
			"1".equals(trimedValue) ||
			"0".equals(trimedValue) ||
			"true".equalsIgnoreCase(trimedValue) ||
			"false".equalsIgnoreCase(trimedValue)
		) {
			objectNode.set(key, JsonNodeFactory.instance.booleanNode(convertToBoolean(trimedValue)));
		} else {
			objectNode.set(key, JsonNodeFactory.instance.textNode(trimedValue));
		}
	}

	/**
	 * Create the a new integer node in the given object node
	 *
	 * @param key The node key
	 * @param value The text value
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createIntegerNode(final String key, final String value, final ObjectNode objectNode) {
		objectNode.set(key, JsonNodeFactory.instance.numberNode(Integer.valueOf(value.trim())));
	}

	/**
	 * Create the a new number node in the given object node
	 *
	 * @param key The node key
	 * @param value The text value
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createNumberNode(final String key, final String value, final ObjectNode objectNode) {
		objectNode.set(key, JsonNodeFactory.instance.numberNode(Double.valueOf(value.trim())));
	}

	/**
	 * Get or create the criteria array node in the given connector node
	 *
	 * @param connector {@link JsonNode} instance
	 * @return {@link JsonNode} of the criteria
	 */
	protected ArrayNode getOrCreateCriteria(final JsonNode connector) {
		final JsonNode connectorSection = connector.get(CONNECTOR);

		ArrayNode criteria;
		if (connectorSection == null) {
			criteria = JsonNodeFactory.instance.arrayNode();
			((ObjectNode) connector).set(
					CONNECTOR,
					JsonNodeFactory.instance
						.objectNode()
						.set(DETECTION, JsonNodeFactory.instance.objectNode().set(CRITERIA, criteria))
				);
			return criteria;
		}

		final JsonNode detection = connectorSection.get(DETECTION);
		if (detection == null) {
			criteria = JsonNodeFactory.instance.arrayNode();
			((ObjectNode) connectorSection).set(DETECTION, JsonNodeFactory.instance.objectNode().set(CRITERIA, criteria));
			return criteria;
		}

		final JsonNode existingCriteria = detection.get(CRITERIA);
		if (existingCriteria == null) {
			criteria = JsonNodeFactory.instance.arrayNode();
			((ObjectNode) detection).set(CRITERIA, criteria);
			return criteria;
		}

		return (ArrayNode) existingCriteria;
	}

	/**
	 * Convert a string of "1" or "0" to boolean
	 *
	 * @param value "1" or "0"
	 * @return converted boolean value
	 */
	private boolean convertToBoolean(final String value) {
		return "1".equals(value) || "true".equalsIgnoreCase(value);
	}

	/**
	 * Create a _comment text entry in the given object node
	 *
	 * @param key The hardware connector key
	 * @param preConnector The {@link PreConnector} providing all the comments
	 * @param node The node we wish to update with the comment
	 */
	protected void appendComment(final String key, final PreConnector preConnector, final ObjectNode node) {
		if (preConnector.getComments().containsKey(key)) {
			final String comments = preConnector.getComments().get(key).stream().collect(Collectors.joining("\n"));
			createTextNode("_comment", comments, node);
		}
	}

	/**
	 * Create a new boolean node in the current source object
	 *
	 * @param key The key of the source context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createSourceBooleanNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode source = getCurrentSource(key, connector);
		createBooleanNode(newNodeKey, value, source);
	}

	/**
	 * Create a new text node in the current source object
	 *
	 * @param key The key of the source context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createSourceTextNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode source = getCurrentSource(key, connector);
		createTextNode(newNodeKey, value, source);
	}

	/**
	 * Create a new array node in the current source object
	 * if it does not exist
	 * or simply adds a value
	 *
	 * @param key        The key of the source context
	 * @param value      The value to create
	 * @param connector  The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createOrAppendToSourceArrayNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode source = getCurrentSource(key, connector);
		final ArrayNode existing = (ArrayNode) source.get(newNodeKey);
		if (existing == null) {
			createStringArrayNode(newNodeKey, new String[] { value }, source);
		} else {
			existing.add(performValueConversions(value));
		}
	}

	/**
	 * Create a new integer node in the current source object
	 *
	 * @param key The key of the source context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createSourceIntegerNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode source = getCurrentSource(key, connector);
		createIntegerNode(newNodeKey, value, source);
	}

	/**
	 * Create a new integer node in the last compute object
	 *
	 * @param key The key of the compute context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createComputeIntegerNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = getCurrentCompute(key, connector);
		createIntegerNode(newNodeKey, value, objectNode);
	}

	/**
	 * Create a new integer node in the last compute object
	 *
	 * @param key The key of the compute context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createComputeNumberNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = getCurrentCompute(key, connector);
		createNumberNode(newNodeKey, value, objectNode);
	}

	/**
	 * Create a new integer node in the last compute object
	 *
	 * @param key The key of the compute context
	 * @param value The value to create
	 * @param connector The whole connector
	 * @param newNodeKey The new node key to create
	 */
	protected void createComputeTextNode(
		final String key,
		final String value,
		final JsonNode connector,
		final String newNodeKey
	) {
		final ObjectNode objectNode = getCurrentCompute(key, connector);
		createTextNode(newNodeKey, value, objectNode);
	}

	/**
	 * Get the current source node.
	 *
	 * @param key The source context key
	 * @param connector The global connector {@link JsonNode}
	 * @return source {@link ObjectNode}. Never <code>null</code>
	 */
	protected ObjectNode getCurrentSource(final String key, final JsonNode connector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final ObjectNode source = getSource(matcher, connector);

		if (source == null) {
			throw new IllegalStateException(String.format("Cannot find source node identified with %s.", key));
		}

		return source;
	}

	/**
	 * Get the current compute node.
	 *
	 * @param key The compute context key
	 * @param connector The global connector {@link JsonNode}
	 * @return source {@link ObjectNode}. Never <code>null</code>
	 */
	protected ObjectNode getCurrentCompute(final String key, final JsonNode connector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final ObjectNode compute = getLastCompute(matcher, connector);

		if (compute == null) {
			throw new IllegalStateException(String.format("Cannot find compute node identified with %s.", key));
		}

		return compute;
	}

	/**
	 * Get or create the attributes node
	 *
	 * @param key The context key
	 * @param connector the global connector {@Link JsonNode}
	 * @return attributesNode {@link ObjectNode}. Never <code>null</code>
	 */
	protected ObjectNode getOrCreateAttributes(final String key, final JsonNode connector) {
		ObjectNode mapping = getOrCreateMapping(key, connector, DISCOVERY);

		final JsonNode attributesNode = mapping.get(ATTRIBUTES);

		if (attributesNode == null) {
			final ObjectNode attributes = JsonNodeFactory.instance.objectNode();
			mapping.set(ATTRIBUTES, attributes);
			return attributes;
		}

		return (ObjectNode) attributesNode;
	}

	/**
	 * get or create mapping node
	 *
	 * @param key the context key
	 * @param connector the global connector {@Link JsonNode}
	 * @param jobType the type of the job: <em>discovery</em> or <em>collect</em>
	 * @return mapping {@link ObjectNode}. Never <code>null</code>
	 */
	protected ObjectNode getOrCreateMapping(final String key, final JsonNode connector, final String jobType) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final JsonNode monitors = connector.get(MONITORS);

		final String monitorName = getMonitorName(matcher);

		if (monitors == null) {
			final ObjectNode mapping = JsonNodeFactory.instance.objectNode();
			// Create the whole hierarchy
			((ObjectNode) connector).set(
					MONITORS,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							monitorName,
							JsonNodeFactory.instance
								.objectNode()
								.set(jobType, JsonNodeFactory.instance.objectNode().set(MAPPING, mapping))
						)
				);
			return mapping;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			final ObjectNode mapping = JsonNodeFactory.instance.objectNode();
			((ObjectNode) monitors).set(
					monitorName,
					JsonNodeFactory.instance
						.objectNode()
						.set(jobType, JsonNodeFactory.instance.objectNode().set(MAPPING, mapping))
				);

			return mapping;
		}

		// Check the job
		final JsonNode job = monitor.get(jobType);
		if (job == null) {
			final ObjectNode mapping = JsonNodeFactory.instance.objectNode();
			((ObjectNode) monitor).set(jobType, JsonNodeFactory.instance.objectNode().set(MAPPING, mapping));
			return mapping;
		}

		final JsonNode mappingNode = job.get(MAPPING);

		if (mappingNode == null) {
			final ObjectNode mapping = JsonNodeFactory.instance.objectNode();
			((ObjectNode) job).set(MAPPING, mapping);
			return mapping;
		}

		return (ObjectNode) mappingNode;
	}

	/**
	 * Extract the mapping attribute name from the given key.<br><br>
	 *
	 * e.g. extract <b>DeviceID</b> from <b>Enclosure.Discovery.Instance.DeviceID</b>.<br>
	 * e.g. extract <b>ParameterActivation.Temperature</b> from <b>Enclosure.Discovery.Instance.ParameterActivation.Temperature</b>.
	 *
	 * @param key	The key from which the parameter name should be extracted.
	 *
	 * @return		The parameter name contained in the given key.
	 */
	protected String getMappingAttribute(final String key) {
		final Matcher matcher = getMatcher(key);

		if (!matcher.find()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		return matcher.group(3);
	}

	/**
	 * Create a source node in the given connector
	 *
	 * @param key The matcher used to determine the monitor name, the job
	 * name and the source name
	 * @param connector The global connector node
	 * @return {@link ObjectNode} instance
	 */
	protected ObjectNode createSource(final String key, final JsonNode connector) {
		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(String.format(INVALID_KEY_MESSAGE_FORMAT, key));
		}

		final JsonNode monitors = connector.get(MONITORS);
		final ObjectNode source = JsonNodeFactory.instance.objectNode();
		final String sourceName = getSourceName(matcher);
		final String jobName = getMonitorJobName(matcher);
		final String monitorName = getMonitorName(matcher);

		if (monitors == null) {
			// Create the whole hierarchy
			((ObjectNode) connector).set(
					MONITORS,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							monitorName,
							JsonNodeFactory.instance
								.objectNode()
								.set(
									jobName,
									JsonNodeFactory.instance
										.objectNode()
										.set(SOURCES, JsonNodeFactory.instance.objectNode().set(sourceName, source))
								)
						)
				);
			return source;
		}

		// Check the monitor
		final JsonNode monitor = monitors.get(monitorName);
		if (monitor == null) {
			((ObjectNode) monitors).set(
					monitorName,
					JsonNodeFactory.instance
						.objectNode()
						.set(
							jobName,
							JsonNodeFactory.instance
								.objectNode()
								.set(SOURCES, JsonNodeFactory.instance.objectNode().set(sourceName, source))
						)
				);
			return source;
		}

		// Check the job
		final JsonNode job = monitor.get(jobName);
		if (job == null) {
			((ObjectNode) monitor).set(
					jobName,
					JsonNodeFactory.instance
						.objectNode()
						.set(SOURCES, JsonNodeFactory.instance.objectNode().set(sourceName, source))
				);
			return source;
		}

		// Check the sources node
		final JsonNode sources = job.get(SOURCES);

		if (sources == null) {
			((ObjectNode) job).set(SOURCES, JsonNodeFactory.instance.objectNode().set(sourceName, source));

			return source;
		}

		((ObjectNode) sources).set(sourceName, source);

		return source;
	}
}
