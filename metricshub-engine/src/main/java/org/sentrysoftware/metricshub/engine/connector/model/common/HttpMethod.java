package org.sentrysoftware.metricshub.engine.connector.model.common;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * An enumeration representing different HTTP methods, including GET, POST, DELETE, and PUT.
 */
@Getter
@AllArgsConstructor
public enum HttpMethod {
	/**
	 * HTTP GET method.
	 */
	GET,
	/**
	 * HTTP POST method.
	 */
	POST,
	/**
	 * HTTP DELETE method.
	 */
	DELETE,
	/**
	 * HTTP PUT method.
	 */
	PUT;

	/**
	 * List of all HTTP methods.
	 */
	public static final List<HttpMethod> HTTP_METHODS = List.of(HttpMethod.values());
}
