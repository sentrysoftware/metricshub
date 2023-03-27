package com.sentrysoftware.matrix.connector.model.common;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum ConversionType {

	@JsonAlias(value = { "hex2Dec", "hex_2_dec" })
	HEX_2_DEC("hex2Dec"),
	@JsonAlias(value = { "array2SimpleStatus", "array_2_simple_status" })
	ARRAY_2_SIMPLE_STATUS("array2SimpleStatus");

	@Getter
	private String name;

	/**
	 * Get {@link ConversionType} by name, the name defined in the connector code
	 * 
	 * @param name The name in the connector defining a {@link ConversionType}. E.g. hex2dec
	 * @return {@link ConversionType} instance
	 */
	public static ConversionType getByName(@NonNull final String name) {
		return Arrays
			.stream(ConversionType.values())
			.filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Undefined conversion type: " + name));
	}
}
