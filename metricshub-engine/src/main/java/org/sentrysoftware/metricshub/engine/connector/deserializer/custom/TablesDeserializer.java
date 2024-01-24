package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Custom deserializer for deserializing a collection of tables.
 */
public class TablesDeserializer extends AbstractNonBlankNonNullInCollectionDeserializer {

	@Override
	protected String getErrorMessage() {
		return "The table referenced by 'TableUnionSource' cannot be empty.";
	}

	@Override
	protected Collection<String> emptyCollection() {
		return new ArrayList<>();
	}

	@Override
	protected Collector<String, ?, Collection<String>> collector() {
		return Collectors.toCollection(ArrayList::new);
	}

	@Override
	protected Collection<String> fromCollection(Collection<String> collection) {
		return new ArrayList<>(collection);
	}
}
