package com.sentrysoftware.matrix.connector.parser.state.source.sshinteractive.step;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;

import lombok.NonNull;

public class DurationProcessor extends StepProcessor {

	private static final Pattern DURATION_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.Step\\(([1-9]\\d*)\\)\\.Duration\\s*$",
			Pattern.CASE_INSENSITIVE);
	
	@Override
	protected Class<?> getType() {
		return ConnectorSleepProperty.TYPE;
	}

	@Override
	protected String getTypeValue() {
		return ConnectorSleepProperty.TYPE_VALUE;
	}

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return DURATION_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((Sleep) getSourceStep(key, connector)).setDuration(Long.valueOf(value));

		} catch (final Exception e) {
			throw new IllegalStateException(String.format("DurationProcessor parse %s, error: %s", value, e.getMessage()), e);
		}
	}

}
