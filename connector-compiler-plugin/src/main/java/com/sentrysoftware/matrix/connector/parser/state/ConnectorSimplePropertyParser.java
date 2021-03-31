package com.sentrysoftware.matrix.connector.parser.state;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ConnectorSimplePropertyParser implements ConnectorStateParser {

	@Override
	public boolean detect(final String key) {

		return key.trim().startsWith("hdf.");
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		ConnectorSimpleProperty.getConnectorSimpleProperties().stream().filter(state -> state.detect(key))
				.forEach(sp -> sp.parse(key, value, connector));
	}

	@Getter
	@AllArgsConstructor
	public enum ConnectorSimpleProperty {

		DISPLAY_NAME(new DisplayNameProcessor()),
		TYPICAL_PLATFORM( new TypicalPlatformProcessor()),
		RELIES_ON(new ReliesOnProcessor()),
		VERSION(new VersionProcessor()),
		REMOTE_SUPPORT(new RemoteSupportProcessor()),
		LOCAL_SUPPORT(new LocalSupportProcessor()),
		APPLIES_TO_OS(new AppliesToOSProcessor()),
		SUPERSEDES(new SupersedesProcessor());

		private ConnectorStateParser connectorStateProcessor;

		public boolean detect(final String key) {

			return connectorStateProcessor.detect(key);
		}

		public void parse(final String key, final String value, final Connector connector) {

			connectorStateProcessor.parse(key, value, connector);
		}

		public static Set<ConnectorSimpleProperty> getConnectorSimpleProperties() {

			return Arrays.stream(ConnectorSimpleProperty.values()).collect(Collectors.toSet());
		}
	}

	public static class DisplayNameProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {
			
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub
			
		}
		
	}

	public static class SupersedesProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class AppliesToOSProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class LocalSupportProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class RemoteSupportProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	public static class TypicalPlatformProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub
			
		}

	}



	public static class ReliesOnProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}

	
	public static class VersionProcessor implements ConnectorStateParser {

		@Override
		public boolean detect(String key) {

			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void parse(String key, String value, Connector connector) {

			// TODO Auto-generated method stub

		}

	}
}
