package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;

public class PropertyNameProcessor extends ExtractPropertyFromWbemPathProcessor {
	
	private static final Pattern PROPERTY_NAME_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.propertyname\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return PROPERTY_NAME_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((ExtractPropertyFromWbemPath) getCompute(key, connector)).setPropertyName(value);
	}
}
