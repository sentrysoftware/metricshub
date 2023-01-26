package com.sentrysoftware.matrix.connector.model.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum HttpMethod {

	GET("get"),
	POST("post"),
	DELETE("delete");

	private static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.values());

	private String name;

	/**
	 * Get {@link HttpMethod} by name, the name defined in the connector
	 * code
	 * 
	 * @param name
	 * @return {@link HttpMethod} instance
	 */
	public static HttpMethod getByName(@NonNull final String name) {
		return HTTP_METHODS.stream().filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined HttpMethod name: " + name));
	}

}
