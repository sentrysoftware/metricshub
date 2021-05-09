package com.sentrysoftware.matrix.connector.parser.state.instance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.InstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.TextInstanceTable;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;

public class InstanceTableProcessor extends AbstractInstanceProcessor {

	/**
	 * Pattern to detect discovery InstanceTable
	 */
	private static final Pattern INSTANCE_TABLE_PATTERN = Pattern.compile("^\\s*([a-z]+)\\.discovery\\.instancetable\\s*$", Pattern.CASE_INSENSITIVE);

	/**
	 * Pattern to extract source types
	 */
	private static final Pattern SOURCE_PATTERN = Pattern.compile("^\\s*%(.*)\\.(discovery|collect)\\.source\\(([0-9]+)\\)%\\s*$", Pattern.CASE_INSENSITIVE);

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		Assert.notNull(connector, "connector cannot be null.");

		// First get the HardwareMonitor to update
		final HardwareMonitor hardwareMonitor = super.getHardwareMonitor(key, connector);

		// Get the instanceTable which is nothing but a source reference
		final InstanceTable instanceTable = getInstanceTableFromValue(value);


		// Simply set the result in the discovery since instanceTable goes in the discovery object
		hardwareMonitor.getDiscovery().setInstanceTable(instanceTable);
	}

	/**
	 * Get the {@link InstanceTable} ({@link SourceInstanceTable} or
	 * {@link TextInstanceTable}) expressed by the given value
	 * 
	 * @param value
	 * @return {@link InstanceTable}
	 */
	InstanceTable getInstanceTableFromValue(final String value) {
		final Matcher matcher = SOURCE_PATTERN.matcher(value);

		return matcher.find() ? getSourceInstanceTable(value) : getTextInstanceTable(value);

	}

	/**
	 * Extract the text from the given value and build a {@link TextInstanceTable} 
	 * @param value
	 * @return {@link TextInstanceTable}
	 */
	TextInstanceTable getTextInstanceTable(final String value) {
		// remove first and last double quote
		return TextInstanceTable.builder().text(value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")).build();
	}

	/**
	 * Extract the source reference defined in the given value then build a {@link SourceInstanceTable} 
	 * @param value
	 * @return  {@link SourceInstanceTable}
	 */
	SourceInstanceTable getSourceInstanceTable(final String value) {

		return SourceInstanceTable.builder().sourceKey(value.replaceAll(ConnectorParserConstants.SOURCE_REFERENCE_REGEX_REPLACEMENT, "$1").toLowerCase()).build();
	}

	@Override
	protected Matcher getMatcher(final String key) {
		return INSTANCE_TABLE_PATTERN.matcher(key);
	}

}
