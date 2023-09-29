package com.sentrysoftware.matrix.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.deserializer.PostDeserializeHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;
import com.sentrysoftware.matrix.connector.update.ConnectorUpdateChain;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MonitorJobsDeserializer extends JsonDeserializer<Map<String, MonitorJob>> {

	public static final ObjectMapper OBJECT_MAPPER = newObjectMapper();

	@Override
	public Map<String, MonitorJob> deserialize(JsonParser parser, DeserializationContext context) throws IOException {
		if (parser == null) {
			return new HashMap<>();
		}

		final JsonNode jsonNode = parser.readValueAs(JsonNode.class);

		if (jsonNode == null) {
			return new HashMap<>();
		}

		final ObjectNode monitorsNode = JsonNodeFactory.instance.objectNode();
		monitorsNode.set("monitors", jsonNode);

		// Parse the connector like it is done by the engine for regular connectors
		final Connector connector = JsonHelper.deserialize(OBJECT_MAPPER, monitorsNode, Connector.class);

		// Create the update chain like it is actually done by the engine
		final ConnectorUpdateChain connectorUpdateChain = ConnectorParser.createUpdateChain();

		// Run the update chain
		if (connectorUpdateChain != null) {
			connectorUpdateChain.update(connector);
		}

		// Here we are going to get the parsing context to get the parent object (ResourceConfig)
		// Once the parent is retrieved, the connector is set in this parent instance.
		// This connector will be used later by the engine to run the monitor jobs
		final JsonStreamContext parsingContext = parser.getParsingContext();
		final Object parent = parsingContext.getCurrentValue();

		((ResourceConfig) parent).setConnector(connector);

		return connector.getMonitors();
	}

	/**
	 * Create a new ObjectMapper for {@link Connector} deserialization
	 *
	 * @return {@link ObjectMapper}
	 */
	private static ObjectMapper newObjectMapper() {
		final ObjectMapper objectMapper = ConfigHelper.newObjectMapper();

		// Post deserialize support creates a task after the initial connector deserialization
		// to update the connector information such as the references that are used later by the update chain
		// in order to create the source dependency tree that the engine uses to process the sources
		// a certain order.
		PostDeserializeHelper.addPostDeserializeSupport(objectMapper);

		return objectMapper;
	}
}
