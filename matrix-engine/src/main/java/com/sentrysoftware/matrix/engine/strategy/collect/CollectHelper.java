package com.sentrysoftware.matrix.engine.strategy.collect;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CollectHelper {

	private CollectHelper() {}

	public static final Pattern VALUE_TABLE_PATTERN = Pattern.compile("^\\s*valuetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final String UNKNOWN_STATUS_LOG_MSG = "For host {}, unexpected status value for instance {}. {} = {}";

	private static final Map<String, ParameterState> PREDICTED_FAILURE_MAP;
	private static final Map<String, ParameterState> STATUS_MAP;
	private static final List<String> MAYBE_NEGATIVE_PARAMETERS;

	static {
		final Map<String, ParameterState> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		map.put("OK", ParameterState.OK);
		map.put("WARN", ParameterState.WARN);
		map.put("ALARM", ParameterState.ALARM);
		map.put("0", ParameterState.OK);
		map.put("1", ParameterState.WARN);
		map.put("2", ParameterState.ALARM);
		map.put("OFF", ParameterState.OK);
		map.put("BLINKING", ParameterState.WARN);
		map.put("ON", ParameterState.ALARM);

		STATUS_MAP = Collections.unmodifiableMap(map);

		final Map<String, ParameterState> predictedFailureMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		map.put("TRUE", ParameterState.WARN);
		map.put("FALSE", ParameterState.OK);

		PREDICTED_FAILURE_MAP = Collections.unmodifiableMap(predictedFailureMap);

		// Update this list when you collect a parameter accepting a negative value
		MAYBE_NEGATIVE_PARAMETERS = Collections.unmodifiableList(Collections.emptyList());
	}

	/**
	 * Translate the status String value to a {@link ParameterState}
	 * 
	 * @param status         Status value in String format (OK, WARN, ALARM, 0, 1, 2, ON, OFF, BLINKING)
	 * @param unknownStatus  Unknown status used when we are not able to translate the collected status
	 * @param monitorId      Current collected monitor identifier
	 * @param hostname       Current hostname  
	 * @param parameterName  The name of the {@link StatusParam} e.g. status, intrusionStatus...
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

		final ParameterState parameterState;
		// Get the parameter state from our PREDICTED_FAILURE_MAP
		if (HardwareConstants.PREDICTED_FAILURE_PARAMETER.equalsIgnoreCase(parameterName)) {
			parameterState = PREDICTED_FAILURE_MAP.get(status.trim());
		} else {
			// Get the parameter state from our STATUS_MAP
			parameterState = STATUS_MAP.get(status.trim());
		}
		

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

	/**
	 * Get the {@link NumberParam} raw value
	 * 
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link NumberParam} rawValue
	 * @param parameterName The name of the {@link NumberParam} instance
	 * @param previous      Indicate whether we should return the <code>rawValue</code> or <code>previousRawValue</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberParamRawValue(final Monitor monitor, final String parameterName, final boolean previous) {

		final NumberParam parameter = monitor.getParameter(parameterName, NumberParam.class);

		if (parameter == null) {
			return null;
		}

		return previous ? getDoubleValue(parameter.getPreviousRawValue()) : getDoubleValue(parameter.getRawValue());
	}

	/**
	 * Get the {@link NumberParam} collect time
	 * 
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link NumberParam} collect time
	 * @param parameterName The name of the {@link NumberParam} instance
	 * @param previous      Indicate whether we should return the <code>collectTime</code> or the <code>previousCollectTime</code>.
	 * @return a {@link Double} value
	 */
	public static Double getNumberParamCollectTime(final Monitor monitor, final String parameterName, final boolean previous) {

		final NumberParam parameter = monitor.getParameter(parameterName, NumberParam.class);

		if (parameter == null) {
			return null;
		}

		return previous ? getDoubleValue(parameter.getPreviousCollectTime()) : getDoubleValue(parameter.getCollectTime());
	}

	/**
	 * Return the {@link Double} value of the given {@link Number} instance
	 * 
	 * @param number
	 * @return {@link Double} instance
	 */
	public static Double getDoubleValue(final Number number) {
		if (number == null) {
			return null;
		}

		return number.doubleValue();
	}

	/**
	 * Perform a subtraction arithmetic operation
	 * 
	 * @param parameterName	The name of the parameter
	 * @param minuend		Minuend of the subtraction
	 * @param subtrahend	Subtrahend of the subtraction
	 * 
	 * @return {@link Double} value
	 */
	public static Double subtract(final String parameterName, final Double minuend, final Double subtrahend) {

		if (minuend == null || subtrahend == null) {
			return null;
		}

		final double result = minuend - subtrahend;

		if (result < 0 && !MAYBE_NEGATIVE_PARAMETERS.contains(parameterName)) {
			log.warn("Suspicious negative value ({} - {}) = {} for parameter {}", minuend, subtrahend, result, parameterName);
			return null;
		}

		return result;

	}

	/**
	 * Perform a division arithmetic operation
	 * 
	 * @param parameter The parameter we wish to compute using a division (Rate, Percentage...)
	 * @param dividend  The dividend to use
	 * @param divisor   The divisor to use
	 * @return {@link Double} value
	 */
	public static Double divide(final String parameter, final Double dividend, final Double divisor) {

		if (dividend == null || divisor == null) {
			return null;
		}

		if (divisor == 0) {
			log.debug("Couldn't compute ({} / {}) for parameter {}", dividend, divisor, parameter);
			return null;
		}

		final double result = dividend / divisor;

		if (result < 0 && !MAYBE_NEGATIVE_PARAMETERS.contains(parameter)) {
			return null;
		}

		return result;
	}

	/**
	 * Perform a multiplication arithmetic operation
	 * 
	 * @param parameter    The parameter we wish to compute using a multiplication
	 * @param multiplier   The multiplier to use
	 * @param multiplicand The multiplicand to use
	 * @return {@link Double} value
	 */
	public static Double multiply(final String parameter, final Double multiplier, final Double multiplicand) {

		if (multiplier == null || multiplicand == null) {
			return null;
		}

		double result = multiplier * multiplicand;

		if (result < 0 && !MAYBE_NEGATIVE_PARAMETERS.contains(parameter)) {
			return null;
		}

		return result;

	}

	/**
	 * Compute a rate (value - previousValue) / (collectTime - previousCollectTime)
	 * 
	 * @param parameterName       The parameter we wish to compute its rate value
	 * @param value               The value from the current collect
	 * @param previousValue       The value from the previous collect
	 * @param collectTime         The time of the current collect
	 * @param previousCollectTime The time of the previous collect
	 *
	 * @return {@link Double} value
	 */
	public static Double rate(String parameterName, Double value, Double previousValue, Double collectTime,
			Double previousCollectTime) {
		return CollectHelper.divide(parameterName, 
				CollectHelper.subtract(parameterName, value, previousValue),
				CollectHelper.subtract(parameterName, collectTime, previousCollectTime));
	}

}
