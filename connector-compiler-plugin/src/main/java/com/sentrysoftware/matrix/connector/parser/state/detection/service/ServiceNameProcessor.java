package com.sentrysoftware.matrix.connector.parser.state.detection.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.NonNull;

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
	public Matcher getMatcher(@NonNull final String key) {
		return SERVICE_NAME_PATTERN.matcher(key);
	}

	@Override
	public void parse(
			final String key,
			final String value,
			final Connector connector) {

		super.parse(key, value, connector);

		((Service) getCriterion(key, connector)).setServiceName(value);
	}

}
