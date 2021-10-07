package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.NonNull;

public class PortProcessor extends AbstractStateParser {

	private static final Pattern PORT_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.Port\\s*$",
			Pattern.CASE_INSENSITIVE);
	
	@Override
	protected Class<?> getType() {
		return ConnectorSshInteractiveProperty.TYPE;
	}

	@Override
	protected String getTypeValue() {
		return ConnectorSshInteractiveProperty.TYPE_VALUE;
	}

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return PORT_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		try {
			((SshInteractive) getCriterion(key, connector)).setPort(Integer.valueOf(value));

		} catch (final Exception e) {
			throw new IllegalStateException(String.format("PortProcessor parse %s, error: ", value, e.getMessage()), e);
		}
	}
}
