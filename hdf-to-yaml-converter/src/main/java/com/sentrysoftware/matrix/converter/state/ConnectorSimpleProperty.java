package com.sentrysoftware.matrix.converter.state;

import static com.sentrysoftware.matrix.converter.ConverterConstants.COMMA;
import static com.sentrysoftware.matrix.converter.ConverterConstants.CONNECTION_TYPES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.CONNECTOR;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DETECTION;
import static com.sentrysoftware.matrix.converter.ConverterConstants.ONE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.TRUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.converter.ConnectorLibraryConverter;
import com.sentrysoftware.matrix.converter.PreConnector;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSimpleProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Stream
			.of(
				new DisplayNameProcessor(),
				new TypicalPlatformProcessor(),
				new ReliesOnProcessor(),
				new VersionProcessor(),
				new RemoteSupportProcessor(),
				new LocalSupportProcessor(),
				new AppliesToOsProcessor(),
				new SupersedesProcessor(),
				new CommentsProcessor(),
				new NoAutoDetectionProcessor(),
				new OnLastResortProcessor()
			)
			.collect(Collectors.toSet());
	}

	/**
	 * Create Connector node if not exists
	 *
	 * @param connector global connector {@link JsonNode}
	 * @return ObjectNode instance
	 */
	private static ObjectNode getOrCreateConnector(final JsonNode connector) {
		JsonNode connectorSection = connector.get(CONNECTOR);
		if (connectorSection == null) {
			connectorSection = JsonNodeFactory.instance.objectNode();
			((ObjectNode) connector).set(CONNECTOR, connectorSection);
		}
		return (ObjectNode) connectorSection;
	}

	/**
	 * Create Detection node if not exists (Parent connector)
	 *
	 * @param connector global connector {@link JsonNode}
	 * @return ObjectNode instance
	 */
	private static ObjectNode getOrCreateDetection(final JsonNode connector) {
		final ObjectNode connectorSection = getOrCreateConnector(connector);
		JsonNode detectionSection = connectorSection.get(DETECTION);
		if (detectionSection == null) {
			detectionSection = JsonNodeFactory.instance.objectNode();
			connectorSection.set(DETECTION, detectionSection);
		}
		return (ObjectNode) detectionSection;
	}

	/**
	 * Create ConntectionTypes node if not exists (Parent: connector.detection)
	 *
	 * @param connector global connector {@link JsonNode}
	 * @return ArrayNode instance
	 */
	private static ArrayNode getOrCreateConnectionTypes(final JsonNode connector) {
		return getOrCreateConnectionTypesNode(getOrCreateDetection(connector));
	}

	/**
	 * Create ConntectionTypes node if not exists
	 *
	 * @param detectionSection global connector {@link JsonNode}
	 * @return ArrayNode instance
	 */
	public static ArrayNode getOrCreateConnectionTypesNode(final ObjectNode detectionSection) {
		JsonNode connectionTypeNode = detectionSection.get(CONNECTION_TYPES);
		if (connectionTypeNode == null) {
			connectionTypeNode = JsonNodeFactory.instance.arrayNode();
			detectionSection.set(CONNECTION_TYPES, connectionTypeNode);
		}
		return (ArrayNode) connectionTypeNode;
	}

	/**
	 * Return true if the given key starts with expected property
	 *
	 * @param key The simple property key
	 * @param value The simple property value
	 * @param expectedProperty The expected property
	 * @return boolean value
	 */
	private static boolean detect(final String key, final String value, final String expectedProperty) {
		return value != null && key != null && key.toLowerCase().startsWith(expectedProperty.toLowerCase());
	}

	public static class DisplayNameProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.displayname");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateConnector(connector).set("displayName", JsonNodeFactory.instance.textNode(value));
		}
	}

	public static class SupersedesProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.supersedes");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			final ArrayNode superseedes = JsonNodeFactory.instance.arrayNode();
			Stream
				.of(value.split(COMMA))
				.map(ConnectorLibraryConverter::getConnectorFilenameNoExtension)
				.forEach(superseedes::add);

			getOrCreateDetection(connector).set("supersedes", superseedes);
		}
	}

	public static class AppliesToOsProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.appliestoos");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			final ArrayNode appliesToOs = JsonNodeFactory.instance.arrayNode();
			Stream.of(value.split(COMMA)).forEach(appliesToOs::add);

			getOrCreateDetection(connector).set("appliesTo", appliesToOs);
		}
	}

	public static class LocalSupportProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.localsupport");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			if (Boolean.parseBoolean(value) || ONE.equals(value)) {
				getOrCreateConnectionTypes(connector).add("local");
			}
		}
	}

	public static class RemoteSupportProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.remotesupport");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			if (Boolean.parseBoolean(value) || ONE.equals(value)) {
				getOrCreateConnectionTypes(connector).add("remote");
			}
		}
	}

	public static class TypicalPlatformProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.typicalplatform");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateConnector(connector).set("platforms", JsonNodeFactory.instance.textNode(value));
		}
	}

	public static class ReliesOnProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.relieson");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateConnector(connector).set("reliesOn", JsonNodeFactory.instance.textNode(value));
		}
	}

	public static class VersionProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.version");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateConnector(connector).set("version", JsonNodeFactory.instance.textNode(value));
		}
	}

	public static class CommentsProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.comments");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateConnector(connector).set("information", JsonNodeFactory.instance.textNode(value));
		}
	}

	public static class NoAutoDetectionProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.noautodetection");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateDetection(connector)
				.set(
					"disableAutoDetection",
					JsonNodeFactory.instance.booleanNode(ONE.equals(value) || TRUE.equalsIgnoreCase(value))
				);
		}
	}

	public static class OnLastResortProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.onlastresort");
		}

		@Override
		public void convert(
			final String key,
			final String value,
			final JsonNode connector,
			final PreConnector preConnector
		) {
			getOrCreateDetection(connector)
				.set("onLastResort", JsonNodeFactory.instance.textNode(ConversionHelper.performValueConversions(value)));
		}
	}
}
