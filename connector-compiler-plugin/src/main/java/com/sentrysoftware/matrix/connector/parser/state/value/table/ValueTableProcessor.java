package com.sentrysoftware.matrix.connector.parser.state.value.table;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

public class ValueTableProcessor implements IConnectorStateParser {

	private static final Pattern VALUE_TABLE_KEY_PATTERN = Pattern.compile("^\\s*(([a-z]+)\\.(collect)\\.(valuetable))\\s*$", Pattern.CASE_INSENSITIVE);

	private Pattern getKeyRegex() {
		return VALUE_TABLE_KEY_PATTERN;
	}

	@Override
	public boolean detect(String key, String value, Connector connector) {
		return value != null
				&& key != null
				&& getKeyRegex().matcher(key).matches();
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {
		notNull(key, "key cannot be null.");
		isTrue(
				getKeyRegex().matcher(key).matches(),
				"The key (" + key + ") does not match the following regex: " + getKeyRegex()
				);
		notNull(value, "value cannot be null.");
		notNull(connector, "Connector cannot be null.");

		final HardwareMonitor hardwareMonitor = getHardwareMonitor(key, connector);

		Collect collect = hardwareMonitor.getCollect();
		if (collect == null) {
			collect = Collect.builder().build();
			hardwareMonitor.setCollect(collect);
		}

		collect.setValueTable(value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1"));
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
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName)).findFirst();

		return hardwareMonitorOpt.isPresent() ? hardwareMonitorOpt.get() : createHardwareMonitor(monitorName, connector);
	}

	/**
	 * Create a {@link HardwareMonitor} for the given monitor and add it to the connector.
	 * @param monitorName
	 * @param connector
	 * @return {@link HardwareMonitor} instance
	 */
	HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		final MonitorType monitorType = MonitorType.getByName(monitorName);

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
		return matcher.group(4);
	}
}
