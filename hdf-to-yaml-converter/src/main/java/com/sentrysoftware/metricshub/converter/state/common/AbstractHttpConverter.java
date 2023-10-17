package com.sentrysoftware.metricshub.converter.state.common;

import com.sentrysoftware.metricshub.engine.connector.model.common.HttpMethod;
import com.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import com.sentrysoftware.metricshub.converter.state.AbstractStateConverter;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHttpConverter extends AbstractStateConverter {

	protected static final Set<String> HTTP_METHODS = HttpMethod.HTTP_METHODS
		.stream()
		.map(method -> method.name().toUpperCase())
		.collect(Collectors.toSet());

	/**
	 * Extract the ResultContent value
	 *
	 * @param key HDF key
	 * @param value HDF value
	 * @return String value
	 */
	protected String extractResultContent(final String key, final String value) {
		try {
			return ResultContent.detect(value).getName();
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("%s. Key context: %s", e.getMessage(), key));
		}
	}

	/**
	 * Extract the HTTP method.
	 *
	 * @param key   HDF key
	 * @param value HDF value
	 * @return String value
	 */
	protected String extractHttpMethod(final String key, final String value) {
		final String httpMethod = value.toUpperCase().trim();
		if (HTTP_METHODS.contains(httpMethod)) {
			return httpMethod;
		} else {
			throw new IllegalArgumentException(String.format("Unknown HTTP method %s encounterd for HDF key %s", value, key));
		}
	}
}
