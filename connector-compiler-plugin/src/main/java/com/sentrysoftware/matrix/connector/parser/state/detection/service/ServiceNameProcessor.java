package com.sentrysoftware.matrix.connector.parser.state.detection.service;

import static org.springframework.util.Assert.notNull;

import java.lang.reflect.InvocationTargetException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public class ServiceNameProcessor extends AbstractStateParser {

	public static final String SERVICE_TYPE_VALUE = "Service";

	private static final Pattern SERVICE_NAME_PATTERN = Pattern.compile("^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.servicename\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public Class<?> getType() {
		return Service.class;
	}

	@Override
	public String getTypeValue() {
		return SERVICE_TYPE_VALUE;
	}

	@Override
	public Matcher getMatcher(final String key) {
		notNull(key, "key cannot be null.");
		return SERVICE_NAME_PATTERN.matcher(key);
	}

	@Override
	public void parse(
			final String key,
			final String value,
			final Connector connector) {

		super.parse(key, value, connector);

		try {
			final Criterion criterion = getCriterion(key, connector);

			criterion.getClass()
			.getMethod("setServiceName", String.class)
			.invoke(criterion, value);

		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new IllegalStateException(String.format(
					"ServiceNameProcessor parse: Cannot invoke setServiceName(%s) on %s Criterion: %s",
					value,
					getType().getSimpleName(),
					e.getMessage()));
		}
	}

}
