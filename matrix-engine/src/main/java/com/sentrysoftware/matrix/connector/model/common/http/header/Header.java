package com.sentrysoftware.matrix.connector.model.common.http.header;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public interface Header extends Serializable {

	Map<String, String> getContent(String username, char[] password, String authenticationToken, String hostname);

	Header copy();

	void update(UnaryOperator<String> updater);

	String description();


	/**
	 * Parse the given string header and build a header {@link Map}
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
	public static Map<String, String> parseHeader(final String header) {
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
}
