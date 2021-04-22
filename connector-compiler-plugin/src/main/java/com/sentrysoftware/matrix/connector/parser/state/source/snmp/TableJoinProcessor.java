package com.sentrysoftware.matrix.connector.parser.state.source.snmp;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class TableJoinProcessor implements IConnectorStateParser {

	protected static final String SNMP_TABLE_KEY_REGEX = "^\\s*(([a-z]+)\\.discovery\\.source\\((\\d+)\\)\\.([a-z]+))\\s*$";

	protected static final String SNMP_TABLE_TYPE_KEY = "type";
	protected static final String TABLE_JOINT_KEY = "tablejoint";
	protected static final String LEFT_TABLE_KEY = "lefttable";
	protected static final String RIGHT_TABLE_KEY = "righttable";
	protected static final String LEFT_KEY_COLUMN_KEY = "leftkeycolumn";
	protected static final String RIGHT_KEY_COLUMN_KEY = "rightkeycolumn";
	protected static final String DEFAULT_RIGHT_LINE_KEY = "defaultrightline";

	protected static final Pattern SNMP_TABLE_KEY_PATTERN = Pattern.compile(SNMP_TABLE_KEY_REGEX, Pattern.CASE_INSENSITIVE);

	protected Pattern getKeyRegex() {
		return SNMP_TABLE_KEY_PATTERN;
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

		final int index = getIndex(key);

		if (key.trim().toLowerCase().endsWith(SNMP_TABLE_TYPE_KEY)) {
			if (TABLE_JOINT_KEY.equals(value.trim().toLowerCase())) {
				// We make sure that the source now exists in the connector for this key
				HardwareMonitor hardwareMonitor = getHardwareMonitor(key, connector);
				final Optional<Source> sourceOpt = hardwareMonitor.getDiscovery().getSources().stream()
						.filter(src -> index == src.getIndex()).findFirst();

				if (sourceOpt.isEmpty()) {
					Source source = new TableJoinSource();
					source.setIndex(index);
					hardwareMonitor.getDiscovery().getSources().add(source);
				}
			}
		}

		else if (key.trim().toLowerCase().endsWith(LEFT_TABLE_KEY)) {
			if (getExistingSource(value, connector).isPresent()) {
				Optional<Source> sourceOpt = getSource(key, connector);
				if (sourceOpt.isPresent()) {
					((TableJoinSource) sourceOpt.get()).setLeftTable(value);
				} else {
					Source source = new TableJoinSource();
					source.setIndex(index);
					((TableJoinSource) source).setLeftTable(value);
					getHardwareMonitor(key, connector).getDiscovery().getSources().add(source);
				}
			}
		}

		else if (key.trim().toLowerCase().endsWith(RIGHT_TABLE_KEY)) {
			if (getExistingSource(value, connector).isPresent()) {
				Optional<Source> sourceOpt = getSource(key, connector);
				if (sourceOpt.isPresent()) {
					((TableJoinSource) sourceOpt.get()).setRightTable(value);
				} else {
					Source source = new TableJoinSource();
					source.setIndex(index);
					((TableJoinSource) source).setRightTable(value);
					getHardwareMonitor(key, connector).getDiscovery().getSources().add(source);
				}
			}
		}

		else if (key.trim().toLowerCase().endsWith(LEFT_KEY_COLUMN_KEY)) {
			Optional<Source> sourceOpt = getSource(key, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setLeftKeyColumn(Integer.parseInt(value));
			} else {
				Source source = new TableJoinSource();
				source.setIndex(index);
				((TableJoinSource) source).setLeftKeyColumn(Integer.parseInt(value));
				getHardwareMonitor(key, connector).getDiscovery().getSources().add(source);
			}
		}

		else if (key.trim().toLowerCase().endsWith(RIGHT_KEY_COLUMN_KEY)) {
			Optional<Source> sourceOpt = getSource(key, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setRightKeyColumn(Integer.parseInt(value));
			} else {
				Source source = new TableJoinSource();
				source.setIndex(index);
				((TableJoinSource) source).setRightKeyColumn(Integer.parseInt(value));
				getHardwareMonitor(key, connector).getDiscovery().getSources().add(source);
			}
		}

		else if (key.trim().toLowerCase().endsWith(DEFAULT_RIGHT_LINE_KEY)) {
			Optional<Source> sourceOpt = getSource(key, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setDefaultRightLine(Arrays.asList(value.split(";", -1)));
			} else {
				Source source = new TableJoinSource();
				source.setIndex(index);
				((TableJoinSource) source).setDefaultRightLine(Arrays.asList(value.split(";", -1)));
				getHardwareMonitor(key, connector).getDiscovery().getSources().add(source);
			}
		}
	}

	/**
	 * Return the {@link index} of the Source of the given key.
	 * @param key The to parse to find the index.
	 * @return {@link index}
	 */
	protected int getIndex(final String key) {
		return Integer.parseInt(key.substring(key.indexOf('(') + 1, key.indexOf(')')));
	}

	/**
	 * Get the {@link hardwareMonitor} instance for the given hdf key and connector.
	 * If the connector doesn't have a HardwareMonitor for the key, creates it and links it to the connector.
	 * @param key
	 * @param connector
	 * @return {@link hardwareMonitor}
	 */
	protected HardwareMonitor getHardwareMonitor(final String key, final Connector connector) {

		final String monitorName = key.trim().substring(0, key.indexOf('.'));

		final Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName)).findFirst();

		return hardwareMonitorOpt.isPresent() ? hardwareMonitorOpt.get() : createHardwareMonitor(monitorName, connector);
	}

	/**
	 * Get the {@link source} instance for the given hdf key and connector.
	 * If the connector doesn't have a HardwareMonitor for the key, creates it and links it to the connector.
	 * Same thing for the source.
	 * @param key
	 * @param connector
	 * @return {@link source}
	 */
	private Optional<Source> getSource(final String key, final Connector connector) {

		final String monitorName = key.trim().substring(0, key.indexOf('.'));

		final Integer index = getIndex(key);

		final Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName)).findFirst();

		if (hardwareMonitorOpt.isPresent()) {
			return hardwareMonitorOpt.get().getDiscovery().getSources().stream()
					.filter(src -> index.equals(src.getIndex())).findFirst();
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Return the source corresponding to the value from the connector, if it already exists.
	 * @param value
	 * @param connector
	 * @return
	 */
	private Optional<Source> getExistingSource(final String value, final Connector connector) {

		final String monitorName = value.trim().substring(value.indexOf("%") + 1, value.indexOf('.'));
		final Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName)).findFirst();

		return hardwareMonitorOpt.isPresent() ? 
				hardwareMonitorOpt.get().getDiscovery().getSources().stream()
				.filter(src -> src.getIndex().equals(getIndex(value))).findFirst()
				: Optional.empty();
	}

	/**
	 * Create a {@link HardwareMonitor} for the given monitor and add it to the connector.
	 * @param monitorName
	 * @param connector
	 * @return {@link HardwareMonitor} instance
	 */
	protected HardwareMonitor createHardwareMonitor(final String monitorName, final Connector connector) {

		final MonitorType monitorType = MonitorType.getByName(monitorName);

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.discovery(Discovery.builder().build())
				.type(monitorType)
				.build();

		// Set the hardware monitor in the connector
		connector.getHardwareMonitors().add(hardwareMonitor);

		return hardwareMonitor;
	}
}
