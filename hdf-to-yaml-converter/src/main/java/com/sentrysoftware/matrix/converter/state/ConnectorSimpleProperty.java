package com.sentrysoftware.matrix.converter.state;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSimpleProperty {

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
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

	private static boolean detect(final String key, final String value, final String property) {
		return value != null && key != null && key.toLowerCase().startsWith(property.toLowerCase());
	}

	public static class DisplayNameProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.displayname");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class SupersedesProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.supersedes");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class AppliesToOsProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.appliestoos");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class LocalSupportProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.localsupport");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class RemoteSupportProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.remotesupport");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class TypicalPlatformProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.typicalplatform");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class ReliesOnProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.relieson");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class VersionProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.version");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class CommentsProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.comments");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}

	public static class NoAutoDetectionProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.noautodetection");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}
	
	public static class OnLastResortProcessor implements IConnectorStateConverter {

		@Override
		public boolean detect(final String key, final String value, final JsonNode connector) {
			return ConnectorSimpleProperty.detect(key, value, "hdf.onLastResort");
		}

		@Override
		public void convert(final String key, final String value, final JsonNode connector) {
			// TODO Implement
		}
	}
}