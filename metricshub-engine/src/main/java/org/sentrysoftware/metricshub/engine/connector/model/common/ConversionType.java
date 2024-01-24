package org.sentrysoftware.metricshub.engine.connector.model.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * Enumeration representing different conversion types used in the connector.
 */
@AllArgsConstructor
public enum ConversionType {
	/**
	 * Converts a hexadecimal string to its decimal equivalent.
	 * This conversion type is also recognized with alternative names: "hex_2_dec".
	 */
	@JsonAlias(value = { "hex2Dec", "hex_2_dec" })
	HEX_2_DEC("hex2Dec"),
	/**
	 * Converts an array to a simple status.
	 * This conversion type is also recognized with alternative names: "array_2_simple_status".
	 */
	@JsonAlias(value = { "array2SimpleStatus", "array_2_simple_status" })
	ARRAY_2_SIMPLE_STATUS("array2SimpleStatus");

	/**
	 * The name of the conversion type as defined in the connector code.
	 */
	@Getter
	private String name;

	/**
	 * Get {@link ConversionType} by name, the name defined in the connector code.
	 *
	 * @param name The name in the connector defining a {@link ConversionType}. E.g. hex2dec
	 * @return {@link ConversionType} instance
	 * @throws IllegalArgumentException if the provided name does not match any defined conversion type.
	 */
	public static ConversionType getByName(@NonNull final String name) {
		return Arrays
			.stream(ConversionType.values())
			.filter(n -> name.equalsIgnoreCase(n.getName()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException("Undefined conversion type: " + name));
	}
}
