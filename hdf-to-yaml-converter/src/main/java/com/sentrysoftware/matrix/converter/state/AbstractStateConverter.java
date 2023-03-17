package com.sentrysoftware.matrix.converter.state;

import static com.sentrysoftware.matrix.converter.ConverterConstants.CONNECTOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.CRITERIA;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DETECTION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DETECTION_DOT_CRITERIA;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DOT;

import java.util.regex.Matcher;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.state.detection.snmp.OidProcessor;

import lombok.NonNull;

public abstract class AbstractStateConverter implements IConnectorStateConverter {

	private static final String INVALID_KEY = "Invalid key: ";

	protected abstract Matcher getMatcher(String key);

	@Override
	public boolean detect(final String key, final String value, final JsonNode connector) {

		return value != null
			&& key != null
			&& (getMatcher(key)).matches()
			&& isAccurateContext(key, value, connector);
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
	 * @param connector	The {@link JsonNode} used to determine the context.
	 *
	 * @return			Whether or not the context evaluation method call evaluates to <i>true</i>.
	 */
	private boolean isAccurateContext(String key, String value, JsonNode connector) {

		if (key.startsWith(DETECTION_DOT_CRITERIA)) {
			return isCriterionContext(value, connector);
		}

		// TODO: manage source and compute contexts
		return false;
	}

	/**
	 *
	 * @param value		The value of the criterion type,
	 *                  in case <i>this</i> is a <i>TypeProcessor</i>
	 * @param connector    The {@link JsonNode} used to retrieve the criterion
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

		if (this instanceof com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor typeProcessor) {

			return typeProcessor.getHdfType().equalsIgnoreCase(value);
		}

		return (this instanceof OidProcessor) || getLastCriterion(connector) != null;
	}

	/**
	 * Get last criterion defined under the <i>connector: detection: criteria</i> path.
	 * @param connector	The {@link JsonNode} whose criterion is being searched for.
	 *
	 * @return			The criterion in the given {@link JsonNode}
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
	 * @return			The criterion matching the given key.
	 */
	protected JsonNode getLastCriterion(final String key, final JsonNode connector) {

		final Matcher matcher = getMatcher(key);
		if (!matcher.matches()) {
			throw new IllegalStateException(INVALID_KEY + key + DOT);
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
	protected void createCriterionTextNode(String key, String value, JsonNode connector, String newNodeKey) {
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
	protected void createCriterionBooleanNode(String key, String value, JsonNode connector, String newNodeKey) {
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
	protected void createCriterionIntegerNode(String key, String value, JsonNode connector, String newNodeKey) {
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
	protected void createCriterionStringArrayNode(String key, String[] arrayValues, JsonNode connector, String newNodeKey) {
		final ObjectNode objectNode = (ObjectNode) getLastCriterion(key, connector);
		createStringArrayNode(newNodeKey, arrayValues, objectNode);
	}

	/**
	 * Create the a new array node with the array values in the given object node
	 * 
	 * @param key The node key
	 * @param arrayValues The array values to add in the new array node
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createStringArrayNode(String key, String[] arrayValues, ObjectNode objectNode) {
		final ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();
		Stream.of(arrayValues).forEach(arrayNode::add);
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
		objectNode.set(key, JsonNodeFactory.instance.textNode(value));
	}

	/**
	 * Create the a new boolean node in the given object node
	 * 
	 * @param key The node key
	 * @param value The text value
	 * @param objectNode The {@link ObjectNode} to update
	 */
	protected void createBooleanNode(final String key, final String value, final ObjectNode objectNode) {
		objectNode.set(key, JsonNodeFactory.instance.booleanNode(convertToBoolean(value.trim())));
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
			((ObjectNode) connector)
				.set(
					CONNECTOR, 
					JsonNodeFactory
						.instance
						.objectNode()
						.set(
							DETECTION,
							JsonNodeFactory
								.instance
								.objectNode()
								.set(CRITERIA, criteria)
						)
				);
			return criteria;
		}

		final JsonNode detection = connectorSection.get(DETECTION);
		if (detection == null) {
			criteria = JsonNodeFactory.instance.arrayNode();
			((ObjectNode) connectorSection)
				.set(
					DETECTION,
					JsonNodeFactory
						.instance
						.objectNode()
						.set(CRITERIA, criteria)
				);
			return criteria;
		}

		JsonNode existingCriteria = detection.get(CRITERIA);
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
	private boolean convertToBoolean(String value) {
		return "1".equals(value) || "true".equalsIgnoreCase(value);
	}

}
