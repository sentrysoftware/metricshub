package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectHelper {

	private CollectHelper() {}


	public static final Pattern VALUE_TABLE_PATTERN = Pattern.compile("^\\s*valuetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final String UNKNOWN_STATUS_LOG_MSG = "For host {}, unexpected status value for instance {}. {} = {}";

	private static final Map<String, ParameterState> STATUS_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	static {
		STATUS_MAP.put("OK", ParameterState.OK);
		STATUS_MAP.put("WARN", ParameterState.WARN);
		STATUS_MAP.put("ALARM", ParameterState.ALARM);
		STATUS_MAP.put("0", ParameterState.OK);
		STATUS_MAP.put("1", ParameterState.WARN);
		STATUS_MAP.put("2", ParameterState.ALARM);
		STATUS_MAP.put("OFF", ParameterState.OK);
		STATUS_MAP.put("BLINKING", ParameterState.WARN);
		STATUS_MAP.put("ON", ParameterState.ALARM);

	}

	/**
	 * Translate the status String value to a {@link ParameterState}
	 * 
	 * @param status         Status value in String format (OK, WARN, ALARM, 0, 1, 2, ON, OFF, BLINKING)
	 * @param unknownStatus  Unknown status used when we are not able to translate the collected status
	 * @param monitorId      Current collected monitor identifier
	 * @param hostname       Current hostname  
	 * @param parameterName  The name of the {@link StatusParam} e.g. status, intrustionStatus...
	 * @return {@link ParameterState} value
	 */
	public static ParameterState translateStatus(final String status, final ParameterState unknownStatus,
			final String monitorId, String hostname,
			final String parameterName) {

		if (status == null) {
			return null;
		}

		Assert.notNull(unknownStatus, "unknownStatus cannot be null.");
		Assert.notNull(monitorId, "monitorId cannot be null.");
		Assert.notNull(hostname, "hostname cannot be null.");
		Assert.notNull(parameterName, "parameterName cannot be null.");

		// Get the parameter state from our STATUS_MAP
		final ParameterState parameterState = STATUS_MAP.get(status.trim());

		// Means it is an unknown status
		if (parameterState == null) {
			switch(unknownStatus) {
			case OK:
				log.debug(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.OK);
				return unknownStatus;
			case WARN:
				log.warn(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.WARN);
				return unknownStatus;
			case ALARM:
			default:
				log.error(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.ALARM);
				return unknownStatus;
			}
			
		}

		return parameterState;

	}

	/**
	 * From the given row extract the value corresponding to the given <code>valueTableColumn</code>
	 * 
	 * @param valueTable       The unique key of the {@link Source} used for debug purpose
	 * @param parameterKey     The unique key of the parameter. E.g. status, statusInformation, intrusionStatus...
	 * @param monitorType      The type of the monitor we wish to collect
	 * @param row              The data which indicate the parameters to collect
	 * @param valueTableColumn The column index formatted as `ValueTable.Column($number)`
	 * @return {@link String} value
	 */
	public static String getValueTableColumnValue(final String valueTable, final String parameterKey,
			final MonitorType monitorType, final List<String> row,
			final String valueTableColumn) {

		if (valueTableColumn == null) {
			return null;
		}

		Assert.notNull(valueTable, "valueTable cannot be null.");
		Assert.notNull(parameterKey, "parameterKey cannot be null.");
		Assert.notNull(monitorType, "monitorType cannot be null.");
		Assert.notNull(row, "row cannot be null.");

		final Matcher matcher = VALUE_TABLE_PATTERN.matcher(valueTableColumn);

		if (matcher.find()) {

			final int columnIndex = Integer.parseInt(matcher.group(1)) - 1;

			if (columnIndex >= 0 && columnIndex < row.size()) {

				return row.get(columnIndex);

			} else {
				log.warn("Collect - Column {} doesn't match the value table source {}. parameterKey {} - row {} - monitorType {}",
						columnIndex,
						valueTable,
						parameterKey,
						row,
						monitorType);
			}

		}

		return null;
	}
}
