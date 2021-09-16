package com.sentrysoftware.matrix.connector.model.common;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
public enum ConversionType {

	HEX_2_DEC("Hex2Dec"),
	ARRAY_2_SIMPLE_STATUS("Array2SimpleStatus");

	@Getter
	private String name;

	/**
	 * Get {@link ConversionType} by name, the name defined in the hardware connector code
	 * 
	 * @param name The name in the connector defining a {@link ConversionType}. E.g. hex2dec
	 * @return {@link ConversionType} instance
	 */
	public static ConversionType getByName(@NonNull final String name) {
		return Arrays.stream(ConversionType.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined conversion type: " + name));
	}
}
