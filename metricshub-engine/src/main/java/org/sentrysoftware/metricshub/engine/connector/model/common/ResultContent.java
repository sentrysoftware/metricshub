package org.sentrysoftware.metricshub.engine.connector.model.common;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing different types of result content that can be extracted from a connector.
 */
@Getter
@AllArgsConstructor
public enum ResultContent {
	/**
	 * Represents HTTP status as result content.
	 */
	@JsonAlias(value = { "httpStatus", "http_status" })
	HTTP_STATUS("httpStatus"),
	/**
	 * Represents header as result content.
	 */
	@JsonAlias("header")
	HEADER("header"),
	/**
	 * Represents body as result content.
	 */
	@JsonAlias("body")
	BODY("body"),
	/**
	 * Represents all content as result content.
	 */
	@JsonAlias("all")
	ALL("all");

	/**
	 * Map each ResultContent with a regular expression that detects it
	 */
	private static final Map<ResultContent, Pattern> DETECTORS = Map.of(
		HTTP_STATUS,
		Pattern.compile("^httpstatus$|^http_status$"),
		HEADER,
		Pattern.compile("^header$"),
		BODY,
		Pattern.compile("^body$"),
		ALL,
		Pattern.compile("^all$")
	);

	private String name;

	/**
	 * Detects {@link ResultContent} using the value defined in the connector code.
	 *
	 * @param value The value to detect.
	 * @return {@link ResultContent} instance.
	 * @throws IllegalArgumentException If the value is not a supported ResultContent.
	 */
	public static ResultContent detect(final String value) {
		// Null returns null
		if (value == null) {
			return null;
		}

		// Check all regex in DETECTORS to see which one matches
		final String lCaseValue = value.trim().toLowerCase();
		for (Map.Entry<ResultContent, Pattern> detector : DETECTORS.entrySet()) {
			if (detector.getValue().matcher(lCaseValue).find()) {
				return detector.getKey();
			}
		}

		// No match => Exception
		throw new IllegalArgumentException(
			"'" + value + "' is not a supported ResultContent. Accepted values are: [ httpStatus, header, body, all ]."
		);
	}
}
