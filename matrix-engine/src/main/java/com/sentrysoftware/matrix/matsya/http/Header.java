package com.sentrysoftware.matrix.matsya.http;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface Header extends Serializable {
	/**
	 * Gets the HTTP header content as {@link Map} and performs macro replacements
	 *
	 * @param username            HTTP username
	 * @param password            HTTP password
	 * @param authenticationToken HTTP authentication token
	 * @param hostname            HTTP server's hostname
	 * @return header entries as {@link Map}
	 */
	Map<String, String> getContent(String username, char[] password, String authenticationToken, String hostname);

	/**
	 * Performs a deep copy
	 *
	 * @return new {@link Header} instance
	 */
	Header copy();

	/**
	 * Updates the actual header attributes
	 *
	 * @param updater updater function
	 */
	void update(UnaryOperator<String> updater);

	/**
	 * Gets the HTTP header string description
	 *
	 * @return string value
	 */
	String description();

	/**
	 * Parses the given string header and builds a header {@link Map}
	 *
	 * @param header Header content as string formatted like the following example:
	 *
	 *    <pre>
	 *     Accept: application/json
	 *     Content-Encoding: utf-8
	 *    </pre>
	 *
	 * @return Map which indexes keys (header keys) to values (header values)
	 */
	private static Map<String, String> parseHeader(final String header) {
		Map<String, String> result = new HashMap<>();
		for (String line : header.split(NEW_LINE)) {
			if (line != null && !line.trim().isEmpty()) {
				String[] tuple = line.split(":", 2);
				isTrue(tuple.length == 2, "Invalid header entry: " + line);

				result.put(tuple[0].trim(), tuple[1].trim());
			}
		}

		return result;
	}

	/**
	 * Replaces each known HTTP macro in the given header then parse the given header to produce a new {@link Map}
	 *
	 * @param header              header content
	 * @param username            The HTTP username
	 * @param password            The HTTP password
	 * @param authenticationToken The HTTP Authentication Token
	 * @param hostname            The HTTP server's hostname
	 * @return header entries as {@link Map}
	 */
	static Map<String, String> resolveAndParseHeader(
		String header,
		String username,
		char[] password,
		String authenticationToken,
		String hostname
	) {
		final String resolvedHeader = HttpMacrosUpdater.update(header, username, password, authenticationToken, hostname);

		return parseHeader(resolvedHeader);
	}
}
