package com.sentrysoftware.matrix.connector.deserializer.custom;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;

public class OsTypeSetDeserializer extends JsonDeserializer<Set<DeviceKind>> {

	@Override
	public Set<DeviceKind> deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return new HashSet<>();
		}

		try {
			if (parser.isExpectedStartArrayToken()) {
				final Set<String> strSet = parser.readValueAs(new TypeReference<Set<String>>() {});

				return Optional.ofNullable(strSet)
					.map(set -> set
						.stream()
						.map(DeviceKind::detect)
						.filter(Objects::nonNull)
						.collect(Collectors.toSet())
					)
					.orElse(new HashSet<>());
			}

			return Optional
				.ofNullable(parser.getValueAsString())
				.map(str -> new HashSet<>(Collections.singleton(DeviceKind.detect(str))))
				.orElse(new HashSet<>());

		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

}
