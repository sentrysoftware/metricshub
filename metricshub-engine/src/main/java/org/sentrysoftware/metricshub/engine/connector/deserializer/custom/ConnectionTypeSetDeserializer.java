package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.sentrysoftware.metricshub.engine.connector.model.identity.ConnectionType;

/**
 * Custom deserializer for converting JSON arrays to a set of {@link ConnectionType}.
 */
public class ConnectionTypeSetDeserializer extends AbstractCollectionDeserializer<ConnectionType> {

	@Override
	protected Function<String, ConnectionType> valueExtractor() {
		return ConnectionType::detect;
	}

	@Override
	protected Collection<ConnectionType> emptyCollection() {
		return new HashSet<>();
	}

	@Override
	protected Collector<ConnectionType, ?, Collection<ConnectionType>> collector() {
		return Collectors.toCollection(HashSet::new);
	}

	@Override
	protected Collection<ConnectionType> fromCollection(Collection<ConnectionType> collection) {
		return new HashSet<>(collection);
	}
}
