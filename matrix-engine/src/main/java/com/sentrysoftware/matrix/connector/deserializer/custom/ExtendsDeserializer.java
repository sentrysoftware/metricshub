package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ExtendsDeserializer extends AbstractNonBlankNonNullInCollectionDeserializer {

	@Override
	protected String getErrorMessage() {
		return "The connector referenced by 'extends' cannot be empty.";
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
