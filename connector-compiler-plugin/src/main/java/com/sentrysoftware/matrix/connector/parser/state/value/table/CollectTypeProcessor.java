package com.sentrysoftware.matrix.connector.parser.state.value.table;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.Optional;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.CollectType;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class CollectTypeProcessor implements IConnectorStateParser {

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile("^\\s*(([a-z]+)\\.(collect)\\.(type))\\s*$", Pattern.CASE_INSENSITIVE);

	private Pattern getKeyRegex() {
		return TYPE_KEY_PATTERN;
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

		if (ConnectorParserConstants.MONO_INSTANCE.equalsIgnoreCase(value)) {
			setCollectType(key, connector, CollectType.MONO_INSTANCE);
		} else if (ConnectorParserConstants.MULTI_INSTANCE.equalsIgnoreCase(value)) {
			setCollectType(key, connector, CollectType.MULTI_INSTANCE);
		}
	}

	/**
	 * Retrieve the hardwareMonitor from the connector and then its collect.
	 * If the hw or the collect doesn't exists, create it.
	 * Then set the type of the collect (MONO_INSTANCE or MULTI_INSTANCE).
	 * @param key
	 * @param connector
	 * @param collectType
	 */
	void setCollectType(String key, Connector connector, CollectType collectType) {
		final HardwareMonitor hardwareMonitor = getHardwareMonitor(key, connector);

		Collect collect = hardwareMonitor.getCollect();
		if (collect == null) {
			collect = Collect.builder().build();
			hardwareMonitor.setCollect(collect);
		}

		collect.setType(collectType);
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
}
