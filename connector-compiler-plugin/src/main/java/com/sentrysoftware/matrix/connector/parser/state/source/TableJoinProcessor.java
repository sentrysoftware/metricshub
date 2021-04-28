package com.sentrysoftware.matrix.connector.parser.state.source;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class TableJoinProcessor implements IConnectorStateParser {


	protected static final String TYPE_KEY = "type";
	protected static final String TABLE_JOINT_KEY = "tablejoint";
	protected static final String LEFT_TABLE_KEY = "lefttable";
	protected static final String RIGHT_TABLE_KEY = "righttable";
	protected static final String LEFT_KEY_COLUMN_KEY = "leftkeycolumn";
	protected static final String RIGHT_KEY_COLUMN_KEY = "rightkeycolumn";
	protected static final String DEFAULT_RIGHT_LINE_KEY = "defaultrightline";

	protected static final Pattern SNMP_TABLE_KEY_PATTERN = Pattern.compile(
			"^\\s*(([a-z]+)\\.(discovery|collect)\\.source\\((\\d+)\\)\\.(type|tablejoint|lefttable|righttable|leftkeycolumn|rightkeycolumn|defaultrightline))\\s*$",
			Pattern.CASE_INSENSITIVE);

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

		final String lowerCaseKey = key.trim().toLowerCase();

		final int index = getIndex(key);
		final String sourceKey = lowerCaseKey.substring(0, lowerCaseKey.indexOf(ConnectorParserConstants.CLOSING_PARENTHESIS) + 1);

		if (lowerCaseKey.endsWith(TYPE_KEY)) {
			if (TABLE_JOINT_KEY.equals(value.trim().toLowerCase())) {
				// We make sure that the source now exists in the connector for this key
				HardwareMonitor hardwareMonitor = getHardwareMonitor(lowerCaseKey, connector);
				MonitorJob monitorJob = getMonitorJob(lowerCaseKey, hardwareMonitor);
				final Optional<Source> sourceOpt = monitorJob.getSources().stream()
						.filter(src -> index == src.getIndex()).findFirst();

				if (sourceOpt.isEmpty()) {
					monitorJob.getSources().add(createSource(index, sourceKey));
				}
			}
		}

		else if (lowerCaseKey.endsWith(LEFT_TABLE_KEY)) {
			Optional<Source> sourceOpt = getSource(lowerCaseKey, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setLeftTable(value);
			} else {
				Source source = createSource(index, sourceKey);
				((TableJoinSource) source).setLeftTable(value);
				getMonitorJob(lowerCaseKey, getHardwareMonitor(lowerCaseKey, connector)).getSources().add(source);
			}
		}

		else if (lowerCaseKey.endsWith(RIGHT_TABLE_KEY)) {
			Optional<Source> sourceOpt = getSource(lowerCaseKey, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setRightTable(value);
			} else {
				Source source =createSource(index, sourceKey);
				((TableJoinSource) source).setRightTable(value);
				getMonitorJob(lowerCaseKey, getHardwareMonitor(lowerCaseKey, connector)).getSources().add(source);
			}
		}

		else if (lowerCaseKey.endsWith(LEFT_KEY_COLUMN_KEY)) {
			Optional<Source> sourceOpt = getSource(lowerCaseKey, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setLeftKeyColumn(Integer.parseInt(value));
			} else {
				Source source = createSource(index, sourceKey);
				((TableJoinSource) source).setLeftKeyColumn(Integer.parseInt(value));
				getMonitorJob(lowerCaseKey, getHardwareMonitor(lowerCaseKey, connector)).getSources().add(source);
			}
		}

		else if (lowerCaseKey.endsWith(RIGHT_KEY_COLUMN_KEY)) {
			Optional<Source> sourceOpt = getSource(lowerCaseKey, connector);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setRightKeyColumn(Integer.parseInt(value));
			} else {
				Source source = createSource(index, sourceKey);
				((TableJoinSource) source).setRightKeyColumn(Integer.parseInt(value));
				getMonitorJob(lowerCaseKey, getHardwareMonitor(lowerCaseKey, connector)).getSources().add(source);
			}
		}

		else if (lowerCaseKey.endsWith(DEFAULT_RIGHT_LINE_KEY)) {
			Optional<Source> sourceOpt = getSource(lowerCaseKey, connector);

			String[] split = (!value.endsWith(ConnectorParserConstants.SEMICOLON) ? value + ConnectorParserConstants.SEMICOLON : value)
					.split(ConnectorParserConstants.SEMICOLON, -1);
			if (sourceOpt.isPresent()) {
				((TableJoinSource) sourceOpt.get()).setDefaultRightLine(Stream.of(split)
						.limit(split.length - 1L)
						.collect(Collectors.toList()));
			} else {
				Source source = createSource(index, sourceKey);
				((TableJoinSource) source).setDefaultRightLine(Stream.of(split)
						.limit(split.length - 1L)
						.collect(Collectors.toList()));
				getMonitorJob(lowerCaseKey, getHardwareMonitor(lowerCaseKey, connector)).getSources().add(source);
			}
		}
	}

	/**
	 * Return the {@link index} of the Source of the given key.
	 * @param key The to parse to find the index.
	 * @return {@link index}
	 */
	protected int getIndex(final String key) {
		return Integer.parseInt(key.substring(key.indexOf(ConnectorParserConstants.OPENING_PARENTHESIS) + 1, key.indexOf(ConnectorParserConstants.CLOSING_PARENTHESIS)));
	}

	/**
	 * Get the {@link hardwareMonitor} instance for the given hdf key and connector.
	 * If the connector doesn't have a HardwareMonitor for the key, creates it and links it to the connector.
	 * @param key
	 * @param connector
	 * @return {@link hardwareMonitor}
	 */
	protected HardwareMonitor getHardwareMonitor(final String key, final Connector connector) {

		final String monitorName = key.substring(0, key.indexOf(ConnectorParserConstants.DOT));

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

		final String monitorName = key.substring(0, key.indexOf(ConnectorParserConstants.DOT));

		final Integer index = getIndex(key);

		final Optional<HardwareMonitor> hardwareMonitorOpt = connector.getHardwareMonitors().stream()
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName)).findFirst();

		if (hardwareMonitorOpt.isPresent()) {
			return getMonitorJob(key, hardwareMonitorOpt.get()).getSources().stream()
					.filter(src -> index.equals(src.getIndex())).findFirst();
		} else {
			return Optional.empty();
		}
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
				.collect(Collect.builder().build())
				.type(monitorType)
				.build();

		// Set the hardware monitor in the connector
		connector.getHardwareMonitors().add(hardwareMonitor);

		return hardwareMonitor;
	}

	/**
	 * Return the hardwareMonitor's discovery or collect job depending on the key.
	 * Since the key has been parsed with the regex, we know it begins with "monitorType.monitorJob".
	 * @param key
	 * @param hardwareMonitor
	 * @return
	 */
	private MonitorJob getMonitorJob(final String key, final HardwareMonitor hardwareMonitor) {
		boolean isCollect = key.substring(key.indexOf(ConnectorParserConstants.DOT) + 1).startsWith(ConnectorParserConstants.COLLECT);
		if (isCollect) {
			Collect collect = hardwareMonitor.getCollect();
			if (collect == null) {
				collect = Collect.builder().build();
				hardwareMonitor.setCollect(collect);
			}
			return collect;
		} else {
			Discovery discovery = hardwareMonitor.getDiscovery();
			if (discovery == null) {
				discovery = Discovery.builder().build();
				hardwareMonitor.setDiscovery(discovery);
			}
			return discovery;
		}
	}

	/**
	 * Create a TableJoinSource and gives it the index and key in parameters.
	 * @param index
	 * @param sourceKey
	 * @return The created source.
	 */
	private Source createSource(final int index, final String sourceKey) {
		Source source = new TableJoinSource();
		source.setIndex(index);
		source.setKey(sourceKey);
		return source;
	}
}
