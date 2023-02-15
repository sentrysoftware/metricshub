package com.sentrysoftware.matrix.connector.model.common;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum EntryConcatMethod implements IEntryConcatMethod {

	LIST("list"),
	JSON_ARRAY("JSONArray"),
	JSON_ARRAY_EXTENDED("JSONArrayExtended");

	private static final EntryConcatMethod[] VALUES = EntryConcatMethod.values();

	private String name;

	/**
	 * Get {@link EntryConcatMethod} by name, the name defined in the connector code
	 * @param name
	 * @return {@link EntryConcatMethod} instance
	 */
	public static EntryConcatMethod getByName(@NonNull final String name) {
		return Arrays
			.stream(VALUES)
			.filter(n -> n.name().equalsIgnoreCase(name) || name.equalsIgnoreCase(n.getName()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Invalid EntryConcatMethod name: " + name));
	}

	@Override
	public IEntryConcatMethod copy() {
		return this;
	}

	@Override
	public String getDescription() {
		return name;
	}
}
