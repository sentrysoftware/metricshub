package org.sentrysoftware.metricshub.engine.client.http;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface Body extends Serializable {
	/**
	 * Gets the HTTP body content as string and performs macro replacements
	 *
	 * @param username            HTTP username
	 * @param password            HTTP password
	 * @param authenticationToken HTTP authentication token
	 * @param hostname            HTTP server's hostname
	 * @return string value
	 */
	String getContent(String username, char[] password, String authenticationToken, String hostname);

	/**
	 * Performs a deep copy
	 *
	 * @return new {@link Body} instance
	 */
	Body copy();

	/**
	 * Updates the actual body attributes
	 *
	 * @param updater updater function
	 */
	void update(UnaryOperator<String> updater);

	/**
	 * Gets the HTTP body string description
	 *
	 * @return string value
	 */
	String description();
}
