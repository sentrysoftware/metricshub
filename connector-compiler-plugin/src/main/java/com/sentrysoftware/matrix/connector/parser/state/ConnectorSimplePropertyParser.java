package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConnectorSimplePropertyParser implements IConnectorStateParser {

	@Override
	public boolean detect(final String key, Connector connector) {

		return key.trim().startsWith("hdf.");
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		ConnectorSimpleProperty.getConnectorSimpleProperties().stream().filter(state -> state.detect(key, connector))
				.forEach(sp -> sp.parse(key, value, connector));
	}

	@Getter
	@AllArgsConstructor
	public enum ConnectorSimpleProperty {

		DISPLAY_NAME(new DisplayNameProcessor()), TYPICAL_PLATFORM(new TypicalPlatformProcessor()),
		RELIES_ON(new ReliesOnProcessor()), VERSION(new VersionProcessor()),
		REMOTE_SUPPORT(new RemoteSupportProcessor()), LOCAL_SUPPORT(new LocalSupportProcessor()),
		APPLIES_TO_OS(new AppliesToOSProcessor()), SUPERSEDES(new SupersedesProcessor());

		private IConnectorStateParser connectorStateProcessor;

		public boolean detect(final String key, Connector connector) {

			return connectorStateProcessor.detect(key, connector);
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
		public boolean detect(String key, Connector connector) {

			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class SupersedesProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class AppliesToOSProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class LocalSupportProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class RemoteSupportProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class TypicalPlatformProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class ReliesOnProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class VersionProcessor implements IConnectorStateParser {

		@Override
		public boolean detect(String key, Connector connector) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}
}
