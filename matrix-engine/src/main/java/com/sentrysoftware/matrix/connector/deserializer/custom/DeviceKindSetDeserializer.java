package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.common.DeviceKind;

public class DeviceKindSetDeserializer extends AbstractHashSetDeserializer<DeviceKind> {

	@Override
	protected Function<String, DeviceKind> valueExtractor() {
		return DeviceKind::detect;
	}

	@Override
	protected Collection<DeviceKind> emptyCollection() {
		return new HashSet<>();
	}

	@Override
	protected Collector<DeviceKind, ?, Collection<DeviceKind>> collector() {
		return Collectors.toCollection(HashSet::new);
	}

	@Override
	protected Collection<DeviceKind> fromCollection(Collection<DeviceKind> collection) {
		return new HashSet<>(collection);
	}
}
