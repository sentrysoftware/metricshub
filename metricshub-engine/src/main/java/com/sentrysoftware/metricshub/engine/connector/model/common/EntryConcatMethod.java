package com.sentrysoftware.metricshub.engine.connector.model.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum EntryConcatMethod implements IEntryConcatMethod {
	@JsonAlias("list")
	LIST("list"),
	@JsonAlias(value = { "jsonArray", "json_array" })
	JSON_ARRAY("jsonArray"),
	@JsonAlias(value = { "JSONArrayExtended", "json_array_extended" })
	JSON_ARRAY_EXTENDED("jsonArrayExtended");

	public static final List<EntryConcatMethod> ENUM_VALUES = List.of(values());

	private String name;

	/**
	 * Get {@link EntryConcatMethod} by name, the name defined in the connector code
	 * @param name
	 * @return {@link EntryConcatMethod} instance
	 */
	public static EntryConcatMethod getByName(@NonNull final String name) {
		return ENUM_VALUES
			.stream()
			.filter(n -> n.name().equalsIgnoreCase(name) || n.getName().equalsIgnoreCase(name))
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
