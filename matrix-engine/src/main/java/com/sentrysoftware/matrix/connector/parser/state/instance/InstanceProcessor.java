package com.sentrysoftware.matrix.connector.parser.state.instance;

import com.sentrysoftware.matrix.connector.model.Connector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceProcessor extends AbstractInstanceProcessor {

	/**
	 * Pattern to detect discovery instance parameters
	 */
	private static final Pattern INSTANCE_PATTERN =
		Pattern.compile("^\\s*([a-z]+)\\.discovery\\.instance\\.(parameteractivation\\.[a-z0-9]+|[a-z0-9]+)\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super
			.getHardwareMonitor(key, connector)
			.getDiscovery()
			.getParameters()
			.put(getParameter(key), value);
	}

	/**
	 * Extract the parameter name from the given key.<br><br>
	 *
	 * e.g. extract <b>DeviceID</b> from <b>Enclosure.Discovery.Instance.DeviceID</b>.<br>
	 * e.g. extract <b>ParameterActivation.Temperature</b> from <b>Enclosure.Discovery.Instance.ParameterActivation.Temperature</b>.
	 *
	 * @param key	The key from which the parameter name should be extracted.
	 *
	 * @return		The parameter name contained in the given key.
	 */
	String getParameter(final String key) {

		final Matcher matcher = getMatcher(key);

		//noinspection ResultOfMethodCallIgnored
		matcher.find();

		return matcher.group(2);
	}

	@Override
	protected Matcher getMatcher(final String key) {
		return INSTANCE_PATTERN.matcher(key);
	}
}
