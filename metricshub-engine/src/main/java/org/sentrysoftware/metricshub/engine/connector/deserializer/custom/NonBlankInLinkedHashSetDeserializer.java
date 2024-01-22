package org.sentrysoftware.metricshub.engine.connector.deserializer.custom;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class NonBlankInLinkedHashSetDeserializer extends AbstractNonBlankNonNullInCollectionDeserializer {

	@Override
	protected String getErrorMessage() {
		return "The value referenced in the collection cannot be empty.";
	}

	@Override
	protected Collection<String> emptyCollection() {
		return new LinkedHashSet<>();
	}

	@Override
	protected Collector<String, ?, Collection<String>> collector() {
		return Collectors.toCollection(LinkedHashSet::new);
	}

	@Override
	protected Collection<String> fromCollection(Collection<String> collection) {
		return new LinkedHashSet<>(collection);
	}
}
