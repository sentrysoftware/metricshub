package org.sentrysoftware.metricshub.engine.client.http;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * Represents the body of an HTTP request.
 */
public interface Body extends Serializable {
	/**
	 * Gets the HTTP body content as a string and performs macro replacements.
	 *
	 * @param username            The HTTP username
	 * @param password            The HTTP password as a character array
	 * @param authenticationToken The HTTP authentication token
	 * @param hostname            The remote hostname
	 * @return The HTTP body content with resolved and parsed HTTP macros
	 */
	String getContent(String username, char[] password, String authenticationToken, String hostname);

	/**
	 * Performs a deep copy of the body.
	 *
	 * @return A new {@link Body} instance with the same content
	 */
	Body copy();

	/**
	 * Updates the actual body attributes using the provided updater function.
	 *
	 * @param updater The function to apply to the current body content
	 */
	void update(UnaryOperator<String> updater);

	/**
	 * Gets a string description of the HTTP body.
	 *
	 * @return The content of the HTTP body as a string
	 */
	String description();
}
