package com.sentrysoftware.matrix.connector.parser.state.value.table;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import lombok.NonNull;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

public class CollectParameterProcessor implements IConnectorStateParser {

	private static final Pattern COLLECT_PARAMETER_KEY_PATTERN = Pattern.compile("^\\s*(([a-z]+)\\.(collect)\\.(?!(type|valuetable))([a-z]+))\\s*$", Pattern.CASE_INSENSITIVE);

	private Pattern getKeyRegex() {
		return COLLECT_PARAMETER_KEY_PATTERN;
	}

	@Override
	public boolean detect(String key, String value, Connector connector) {
		return value != null
				&& key != null
				&& getKeyRegex().matcher(key).matches();
	}

	@Override
	public void parse(@NonNull final String key, @NonNull final String value, @NonNull final Connector connector) {
		isTrue(
				getKeyRegex().matcher(key).matches(),
				"The key (" + key + ") does not match the following regex: " + getKeyRegex()
				);

		final HardwareMonitor hardwareMonitor = getHardwareMonitor(key, connector);

		Collect collect = hardwareMonitor.getCollect();
		if (collect == null) {
			collect = Collect.builder().build();
			hardwareMonitor.setCollect(collect);
		}

		collect.getParameters().put(getParameter(key), value);
	}

	/**
	 * Get the {@link hardwareMonitor} instance for the given hdf key and connector.
	 * If the connector doesn't have a HardwareMonitor for the key, creates it and links it to the connector.
	 * @param key
	 * @param connector
	 * @return {@link hardwareMonitor}
	 */
	HardwareMonitor getHardwareMonitor(final String key, final Connector connector) {

		final String monitorName = key.substring(0, key.indexOf(ConnectorParserConstants.DOT));

		final Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().getNameInConnector().equalsIgnoreCase(monitorName)).findFirst();

		return hardwareMonitorOpt.isPresent() ? hardwareMonitorOpt.get() : createHardwareMonitor(monitorName, connector);
	}

	/**
	 * Create a {@link HardwareMonitor} for the given monitor and add it to the connector.
	 * @param monitorName
	 * @param connector
	 * @return {@link HardwareMonitor} instance
	 */
	HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		final MonitorType monitorType = MonitorType.getByNameInConnector(monitorName);

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.collect(Collect.builder().build())
				.type(monitorType)
				.build();

		// Set the hardware monitor in the connector
		connector.getHardwareMonitors().add(hardwareMonitor);

		return hardwareMonitor;
	}

	/**
	 * Return the parameter associated to a given key
	 * @param key
	 * @return
	 */
	String getParameter(final String key) {
		final Matcher matcher = getKeyRegex().matcher(key);
		matcher.find();
		return matcher.group(5);
	}
}
