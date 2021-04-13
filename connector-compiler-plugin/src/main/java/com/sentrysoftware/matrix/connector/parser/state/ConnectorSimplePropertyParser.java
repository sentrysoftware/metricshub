package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConnectorSimplePropertyParser implements IConnectorStateParser {

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {

		return key != null && key.trim().startsWith("hdf.");
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		ConnectorSimpleProperty.getConnectorSimpleProperties().stream().filter(state -> state.detect(key, value, connector))
				.forEach(sp -> sp.parse(key, value, connector));
	}

	private static boolean detect(final String key, final String value, final Connector connector, final String property) {
		return value != null && key != null && key.toLowerCase().startsWith(property.toLowerCase());
	}

	@Getter
	@AllArgsConstructor
	public enum ConnectorSimpleProperty {

		DISPLAY_NAME(new DisplayNameProcessor()),
		TYPICAL_PLATFORM(new TypicalPlatformProcessor()),
		RELIES_ON(new ReliesOnProcessor()),
		VERSION(new VersionProcessor()),
		REMOTE_SUPPORT(new RemoteSupportProcessor()),
		LOCAL_SUPPORT(new LocalSupportProcessor()),
		APPLIES_TO_OS(new AppliesToOSProcessor()),
		SUPERSEDES(new SupersedesProcessor());

		private IConnectorStateParser connectorStateProcessor;

		public boolean detect(final String key, final String value, final Connector connector) {

			return connectorStateProcessor.detect(key, value, connector);
		}

		public void parse(final String key, final String value, final Connector connector) {

			connectorStateProcessor.parse(key, value, connector);
		}

		public static Set<ConnectorSimpleProperty> getConnectorSimpleProperties() {

			return Arrays.stream(ConnectorSimpleProperty.values()).collect(Collectors.toSet());
		}
	}

	public static class DisplayNameProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.displayname");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setDisplayName(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim());
			}
		}
	}

	public static class SupersedesProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.supersedes");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				Set<String> supersedes = new HashSet<>();

				Arrays.stream(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).split(ConnectorParserConstants.COMA))
					.forEach(supersedesStr -> supersedes.add(supersedesStr.trim()));

				connector.setSupersedes(supersedes);
			}
		}
	}

	public static class AppliesToOSProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.appliestoos");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				Set<OSType> osTypes = new HashSet<>();

				Arrays.stream(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).split(ConnectorParserConstants.COMA))
					.forEach(osTypeStr -> osTypes.add(OSType.valueOf(osTypeStr.trim().toUpperCase())));

				connector.setAppliesToOS(osTypes);
			}
		}
	}

	public static class LocalSupportProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.localsupport");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setLocalSupport(Boolean.parseBoolean(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim().toLowerCase()));
			}
		}
	}

	public static class RemoteSupportProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.remotesupport");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setRemoteSupport(Boolean.parseBoolean(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim().toLowerCase()));
			}
		}
	}

	public static class TypicalPlatformProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.typicalplatform");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setTypicalPlatform(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim());
			}
		}
	}

	public static class ReliesOnProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.relieson");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setReliesOn(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim());
			}
		}
	}

	public static class VersionProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(final String key, final String value, final Connector connector) {
			return ConnectorSimplePropertyParser.detect(key, value, connector, "hdf.version");
		}

		@Override
		public void parse(final String key, final String value, final Connector connector) {
			if (connector != null && value != null) {
				connector.setVersion(value.replace(ConnectorParserConstants.DOUBLE_QUOTE, ConnectorParserConstants.EMPTY_STRING).trim());
			}
		}
	}
}
