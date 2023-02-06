package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public abstract class AbstractHashSetDeserializer<T> extends AbstractCollectionDeserializer<T> {

	@Override
	protected Collection<T> emptyCollection() {
		return new HashSet<>();
	}

	@Override
	protected Collector<T, ?, Collection<T>> collector() {
		return Collectors.toCollection(HashSet::new);
	}

	@Override
	protected Collection<T> fromCollection(Collection<T> collection) {
		return new HashSet<>(collection);
	}

}
