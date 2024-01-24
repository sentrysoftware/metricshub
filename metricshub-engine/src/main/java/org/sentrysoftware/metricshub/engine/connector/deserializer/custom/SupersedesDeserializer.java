package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Custom deserializer for deserializing a collection of supersedes connectors.
 */
public class SupersedesDeserializer extends AbstractNonBlankNonNullInCollectionDeserializer {

	@Override
	protected String getErrorMessage() {
		return "The connector referenced by 'supersedes' cannot be empty.";
	}

	@Override
	protected Collection<String> emptyCollection() {
		return new HashSet<>();
	}

	@Override
	protected Collector<String, ?, Collection<String>> collector() {
		return Collectors.toCollection(HashSet::new);
	}

	@Override
	protected Collection<String> fromCollection(Collection<String> collection) {
		return new HashSet<>(collection);
	}
}
