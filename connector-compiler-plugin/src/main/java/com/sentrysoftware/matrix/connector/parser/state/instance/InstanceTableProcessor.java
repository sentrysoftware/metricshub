package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.InstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.TextInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

public class InstanceTableProcessor extends AbstractInstanceProcessor {

	/**
	 * Pattern to detect discovery InstanceTable
	 */
	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile("^([a-z]+)\\.discovery\\.instancetable$", Pattern.CASE_INSENSITIVE);

	/**
	 * Pattern to extract source types
	 */
	private static final Pattern SOURCE_PATTERN = Pattern.compile("^%(.*)\\.(discovery|collect)\\.source\\(([0-9]+)\\)%$", Pattern.CASE_INSENSITIVE);

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		Assert.notNull(connector, "connector cannot be null.");

		// First get the HardwareMonitor to update
		final HardwareMonitor hardwareMonitor = super.getHardwareMonitor(key, connector);

		// Get the instanceTable which is nothing but a source reference
		final InstanceTable instanceTable = getInstanceTableFromValue(value, connector);


		// Simply set the result in the discovery since instanceTable goes in the discovery object
		hardwareMonitor.getDiscovery().setInstanceTable(instanceTable);
	}

	/**
	 * Get the {@link InstanceTable} ({@link SourceInstanceTable} or
	 * {@link TextInstanceTable}) expressed by the given value
	 * 
	 * @param value
	 * @param connector
	 * @return {@link InstanceTable}
	 */
	protected InstanceTable getInstanceTableFromValue(final String value, final Connector connector) {
		final Matcher matcher = SOURCE_PATTERN.matcher(value);

		return matcher.find() ? getSourceInstanceTable(connector, matcher) : getTextInstanceTable(value);

	}

	/**
	 * Extract the text from the given value and build a {@link TextInstanceTable} 
	 * @param value
	 * @return {@link TextInstanceTable}
	 */
	protected TextInstanceTable getTextInstanceTable(final String value) {
		// remove first and last double quote
		return TextInstanceTable.builder().text(value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")).build();
	}

	/**
	 * Extract the source reference defined in the given value then build a {@link SourceInstanceTable} 
	 * @param connector
	 * @param matcher
	 * @return  {@link SourceInstanceTable}
	 */
	protected SourceInstanceTable getSourceInstanceTable(final Connector connector, final Matcher matcher) {

		final String monitorName = matcher.group(1);
		final String discoveryOrCollect = matcher.group(2);
		final String sourceIndex = matcher.group(3);

		// Get the current hardware monitor and extract the monitor based on the monitor
		// name defined in the value
		final HardwareMonitor hardwareMonitors = connector
				.getHardwareMonitors()
				.stream()
				.filter(hm -> hm.getType().getName().equalsIgnoreCase(monitorName))
				.findFirst()
				.orElse(null);

		 // TODO Remove when all the parsers are ready and replace orElse(null) by the orElseThrow()
		if (hardwareMonitors == null)
			return SourceInstanceTable.builder().build();
		 // .orElseThrow(() -> new IllegalStateException(
				// String.format("Couldn't find the hardwareMonitor %s in the current connector state.", monitorName)))
		 

		// MonitorJob (Discovery or Collect)
		final MonitorJob monitorJob = ConnectorParserConstants.DISCOVERY.equalsIgnoreCase(discoveryOrCollect) ? hardwareMonitors.getDiscovery()
				: hardwareMonitors.getCollect();

		// TODO Remove when all the parsers are ready and execute the Assert.state
		if (monitorJob == null) return SourceInstanceTable.builder().build();
		// Assert.state(monitorJob != null, () -> String.format("Couldn't find the monitorJob for the InstanceTable value %s", value))

		final Source result = monitorJob
				.getSources()
				.stream()
				.filter(source -> source
						.getIndex()
						.equals(Integer.valueOf(sourceIndex)))
				.findFirst().orElse(null); // TODO When all the parsers are ready replace orElse(null) by the orElseThrow()
			  //.orElseThrow(() -> new IllegalStateException(
					//String.format("Couldn't find the source %s in the current connector state.", value)))

		return SourceInstanceTable.builder().source(result).build();
	}

	@Override
	protected Matcher getMatcher(final String key) {
		return INSTANCE_TABLE_PATTERN.matcher(key);
	}

}
