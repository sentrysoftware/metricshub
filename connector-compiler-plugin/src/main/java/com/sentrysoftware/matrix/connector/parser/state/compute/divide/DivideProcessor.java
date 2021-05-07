package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.List;
import java.util.regex.Matcher;

import static org.springframework.util.Assert.notNull;

public abstract class DivideProcessor implements IConnectorStateParser {

	protected static final String DIVIDE_TYPE_VALUE = "Divide";

	protected abstract Matcher getMatcher(String key);

	@Override
	public boolean detect(final String key, final String value, final Connector connector) {

		Matcher matcher;

		return value != null
			&& key != null
			&& (matcher = getMatcher(key)).matches() //NOSONAR - Assigning matcher on purpose
			&& isDivideContext(value, matcher, connector);
	}

	/**
	 * @param value
	 * @param matcher
	 * @param connector	The {@link Connector}
	 *
	 * @return
	 */
	private boolean isDivideContext(String value, Matcher matcher, Connector connector) {

		if (this instanceof TypeProcessor) {

			return DIVIDE_TYPE_VALUE.equalsIgnoreCase(
				value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1"));
		}

		return getDivide(matcher, connector) != null;
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		notNull(key, "key cannot be null.");
		notNull(value, "value cannot be null.");
		notNull(connector, "Connector cannot be null.");
	}

	private Divide getDivide(Matcher matcher, Connector connector) {

		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, getMonitorName(matcher));

		Source source = getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));

		return getDivide(source, getComputeIndex(matcher));
	}

	/**
	 *
	 * @param source
	 * @param computeIndex
	 * @return
	 */
	protected Divide getDivide(Source source, int computeIndex) {

		return source == null
			? null
			: getDivide(source.getComputes(), computeIndex);
	}

	/**
	 *
	 * @param computes
	 * @param computeIndex
	 * @return
	 */
	private Divide getDivide(List<Compute> computes, int computeIndex) {

		if (computes == null) {
			return null;
		}

		return (Divide) computes
			.stream()
			.filter(compute -> compute instanceof Divide && compute.getIndex() == computeIndex)
			.findFirst()
			.orElse(null);
	}

	/**
	 *
	 * @param matcher
	 * @param connector
	 * @return
	 */
	protected Source getSource(Matcher matcher, Connector connector) {

		String monitorName = getMonitorName(matcher);
		HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName);

		return getSource(hardwareMonitor, getMonitorJobName(matcher), getSourceIndex(matcher));
	}

	/**
	 *
	 * @param hardwareMonitor
	 * @param monitorJobName
	 * @param sourceIndex
	 * @return
	 */
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

	/**
	 *
	 * @param connector
	 * @param monitorName
	 * @return
	 */
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

	/**
	 *
	 * @param matcher
	 * @return
	 */
	private String getMonitorName(Matcher matcher) {

		return matcher.group(1);
	}

	/**
	 *
	 * @param matcher
	 * @return
	 */
	private String getMonitorJobName(Matcher matcher) {

		return matcher.group(2);
	}

	/**
	 *
	 * @param matcher
	 * @return
	 */
	private Integer getSourceIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(3));
	}

	/**
	 *
	 * @param matcher
	 * @return
	 */
	protected Integer getComputeIndex(Matcher matcher) {

		return Integer.parseInt(matcher.group(4));
	}
}
