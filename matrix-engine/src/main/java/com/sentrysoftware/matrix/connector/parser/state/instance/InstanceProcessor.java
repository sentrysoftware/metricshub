package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;

public class InstanceProcessor extends AbstractInstanceProcessor {

	/**
	 * Pattern to detect discovery instance parameters
	 */
	private static final Pattern INSTANCE_PATTERN = Pattern.compile("^\\s*([a-z]+)\\.discovery\\.instance\\.([a-z0-9]+)\\s*$", Pattern.CASE_INSENSITIVE);

	
	@Override
	public void parse(final String key, final String value, final Connector connector) {
		final HardwareMonitor hardwareMonitor = super.getHardwareMonitor(key, connector);

		hardwareMonitor.getDiscovery().getParameters().put(getParameter(key), value);
	}


	/**
	 * Extract the parameter name from the key. E.g. extract DeviceID from Enclosure.Discovery.Instance.DeviceID
	 * @param key
	 * @return {@link String} value
	 */
	String getParameter(final String key) {
		final Matcher matcher = getMatcher(key);
		matcher.find();
		return matcher.group(2);
	}


	@Override
	protected Matcher getMatcher(final String key) {
		return INSTANCE_PATTERN.matcher(key);
	}

}
