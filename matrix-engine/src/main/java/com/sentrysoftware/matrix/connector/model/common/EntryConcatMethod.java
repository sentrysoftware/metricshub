package com.sentrysoftware.matrix.connector.model.common;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum EntryConcatMethod {

	LIST("list"),
	JSON_ARRAY("JSONArray"),
	JSON_ARRAY_EXTENDED("JSONArrayExtended"),
	CUSTOM("custom");

	private String name;

	/**
	 * Get {@link EntryConcatMethod} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link EntryConcatMethod} instance
	 */
	public static EntryConcatMethod getByName(@NonNull final String name) {
		return Arrays.stream(EntryConcatMethod.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined EntryConcatMethod name: " + name));
	}
}
