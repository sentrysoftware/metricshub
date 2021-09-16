package com.sentrysoftware.matrix.connector.model.common.http;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum ResultContent {

	HTTP_STATUS("httpStatus"),
	HEADER("header"),
	BODY("body"),
	ALL("all");

	private String name;

	/**
	 * Get {@link ResultContent} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link ResultContent} instance
	 */
	public static ResultContent getByName(@NonNull final String name) {
		return Arrays.stream(ResultContent.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined ResultContent name: " + name));
	}

}
