package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.List;
import java.util.regex.Matcher;

import static org.springframework.util.Assert.notNull;

public abstract class DuplicateColumnProcessor implements IConnectorStateParser {

	protected static final String DUPLICATE_COLUMN_TYPE_VALUE = "DuplicateColumn";

	protected abstract Matcher getMatcher(String key);

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {

		Matcher matcher;

		return value != null
			&& key != null
			&& (matcher = getMatcher(key)).matches() //NOSONAR - Assigning matcher on purpose
			&& isDuplicateColumnContext(value, matcher, connector);
	}

	private boolean isDuplicateColumnContext(String value, Matcher matcher, Connector connector) {

		if (this instanceof TypeProcessor) {

			return DUPLICATE_COLUMN_TYPE_VALUE.equalsIgnoreCase(
				value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1"));
		}

		return getDuplicateColumn(matcher, connector) != null;
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		notNull(key, "key cannot be null.");
		notNull(value, "value cannot be null.");
		notNull(connector, "Connector cannot be null.");
	}

	private DuplicateColumn getDuplicateColumn(Matcher matcher, Connector connector) {

		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, getMonitorName(matcher));

		Source source = getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));

		return getDuplicateColumn(source, getComputeIndex(matcher));
	}

	protected DuplicateColumn getDuplicateColumn(Source source, int computeIndex) {

		return source == null
			? null
			: getDuplicateColumn(source.getComputes(), computeIndex);
	}

	private DuplicateColumn getDuplicateColumn(List<Compute> computes, int computeIndex) {

		if (computes == null) {
			return null;
		}

		return (DuplicateColumn) computes
			.stream()
			.filter(compute -> compute instanceof DuplicateColumn && compute.getIndex() == computeIndex)
			.findFirst()
			.orElse(null);
	}

	protected Source getSource(Matcher matcher, Connector connector) {

		String monitorName = getMonitorName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName);

		return getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	}

	private Source getSource(HardwareMonitor hardwareMonitor, String monitorJobName, int sourceIndex) {

		if (hardwareMonitor == null) {
			return null;
		}

		MonitorJob monitorJob = ConnectorParserConstants.DISCOVERY.equalsIgnoreCase(monitorJobName)
			? hardwareMonitor.getDiscovery()
			: hardwareMonitor.getCollect();

		if (monitorJob == null) {
			return null;
		}

		List<Source> sources = monitorJob.getSources();
		if (sources == null) {
			return null;
		}

		return sources
			.stream()
			.filter(source -> source.getIndex() == sourceIndex)
			.findFirst()
			.orElse(null);
	}

	private HardwareMonitor getHardwareMonitor(Connector connector, String monitorName) {

		notNull(connector, "Connector cannot be null.");

		return connector
			.getHardwareMonitors()
			.stream()
			.filter(hardwareMonitor -> hardwareMonitor
				.getType()
				.getName()
				.equalsIgnoreCase(monitorName))
			.findFirst()
			.orElse(null);
	}

	private String getMonitorName(Matcher matcher) {

		return matcher.group(1);
	}

	private String getMonitorJobName(Matcher matcher) {

		return matcher.group(2);
	}

	private Integer getSourceIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(3));
	}

	protected Integer getComputeIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(4));
	}
}
