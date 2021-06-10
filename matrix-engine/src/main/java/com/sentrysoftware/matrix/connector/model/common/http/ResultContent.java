package com.sentrysoftware.matrix.connector.model.common.http;

import java.util.Arrays;

import org.springframework.util.Assert;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
	public static ResultContent getByName(final String name) {
		Assert.notNull(name, "name cannot be null.");
		return Arrays.stream(ResultContent.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined ResultContent name: " + name));
	}

}
