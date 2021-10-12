package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import lombok.NonNull;
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

		STATUS_MAP = Collections.unmodifiableMap(map);

		final Map<String, ParameterState> predictedFailureMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		predictedFailureMap.put("TRUE", ParameterState.WARN);
		predictedFailureMap.put("FALSE", ParameterState.OK);
		predictedFailureMap.put("1", ParameterState.WARN);
		predictedFailureMap.put("0", ParameterState.OK);

		PREDICTED_FAILURE_MAP = Collections.unmodifiableMap(predictedFailureMap);

		// Update this list when you collect a parameter accepting a negative value
		MAYBE_NEGATIVE_PARAMETERS = Collections.unmodifiableList(Collections.emptyList());
	}

	/**
	 * Translate the status String value to a {@link ParameterState}
	 *
	 * @param status         Status value in String format (OK, WARN, ALARM, 0, 1, 2)
	 * @param unknownStatus  Unknown status used when we are not able to translate the collected status
	 * @param monitorId      Current collected monitor identifier
	 * @param hostname       Current hostname
	 * @param parameterName  The name of the {@link StatusParam} e.g. status, intrusionStatus...
	 * @return {@link ParameterState} value
	 */
	public static ParameterState translateStatus(final String status, @NonNull final Optional<ParameterState> unknownStatus,
			@NonNull final String monitorId, @NonNull String hostname,
			@NonNull final String parameterName) {

		if (status == null) {
			return null;
		}

		final ParameterState parameterState;
		// Get the parameter state from our PREDICTED_FAILURE_MAP
		if (PREDICTED_FAILURE_PARAMETER.equalsIgnoreCase(parameterName)) {
			parameterState = PREDICTED_FAILURE_MAP.get(status.trim());
		} else {
			// Get the parameter state from our STATUS_MAP
			parameterState = STATUS_MAP.get(status.trim());
		}


		// Means it is an unknown status
		if (parameterState == null && unknownStatus.isPresent()) {
			switch(unknownStatus.get()) {
			case OK:
				log.debug(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.OK);
				return unknownStatus.get();
			case WARN:
				log.warn(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.WARN);
				return unknownStatus.get();
			case ALARM:
			default:
				log.error(UNKNOWN_STATUS_LOG_MSG, hostname, monitorId, parameterName, ParameterState.ALARM);
				return unknownStatus.get();
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
	public static String getValueTableColumnValue(@NonNull final String valueTable, @NonNull final String parameterKey,
			@NonNull final MonitorType monitorType, @NonNull final List<String> row,
			final String valueTableColumn) {

		if (valueTableColumn == null) {
			return null;
		}

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

		} else {
			return valueTableColumn;
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
	 * Get the {@link NumberParam} value
	 *
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link NumberParam} value
	 * @param parameterName The name of the {@link NumberParam} instance
	 * @return a {@link Double} value
	 */
	public static Double getNumberParamValue(final Monitor monitor, final String parameterName) {

		final NumberParam parameter = monitor.getParameter(parameterName, NumberParam.class);

		if (parameter == null) {
			return null;
		}

		return getDoubleValue(parameter.getValue());
	}

	/**
	 * Get the {@link StatusParam} state value
	 *
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link StatusParam} state
	 * @param parameterName The name of the {@link StatusParam} instance
	 * @return a {@link ParameterState} value (OK, WARN or ALARM)
	 */
	public static ParameterState getStatusParamState(final Monitor monitor, final String parameterName) {

		final StatusParam parameter = monitor.getParameter(parameterName, StatusParam.class);

		if (parameter == null) {
			return null;
		}

		return parameter.getState();
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

	/**
	 * Update the number parameter value identified by <code>parameterName</code> in the given {@link Monitor} instance
	 *
	 * @param monitor       The monitor we wish to collect the number parameter value
	 * @param parameterName The unique name of the parameter
	 * @param unit          The unit of the parameter
	 * @param collectTime   The collect time for this parameter
	 * @param value         The value to set on the {@link NumberParam} instance
	 * @param rawValue      The raw value to set as it is needed when computing delta and rates
	 */
	public static void updateNumberParameter(@NonNull final Monitor monitor, @NonNull final String parameterName,
			final String unit, @NonNull final Long collectTime,
			final Double value, final Double rawValue) {

		// GET the existing number parameter and update the value and the collect time
		NumberParam numberParam = monitor.getParameter(parameterName, NumberParam.class);

		// The parameter is not present then create it
		if (numberParam == null) {
			numberParam = NumberParam
					.builder()
					.name(parameterName)
					.unit(unit)
					.build();

		}

		numberParam.setValue(value);
		numberParam.setCollectTime(collectTime);
		numberParam.setRawValue(rawValue);

		monitor.collectParameter(numberParam);
	}


	/**
	 * Update the status parameter value identified by <code>parameterName</code> in the given {@link Monitor} instance
	 *
	 * @param monitor           The monitor we wish to collect the status parameter value
	 * @param parameterName     The unique name of the parameter
	 * @param unit              The unit of the parameter
	 * @param collectTime       The collect time for this parameter
	 * @param state             The {@link ParameterState} (OK, WARN, ALARM) used to build the {@link StatusParam}
	 * @param statusInformation The status information
	 */
	public static void updateStatusParameter(@NonNull final Monitor monitor, @NonNull final String parameterName,
			@NonNull final String unit, @NonNull final Long collectTime,
			final ParameterState state, final String statusInformation) {

		StatusParam statusParam = monitor.getParameter(parameterName, StatusParam.class);

		if (statusParam == null) {
			statusParam = StatusParam
					.builder()
					.name(parameterName)
					.unit(unit)
					.build();
		}

		statusParam.setState(state);
		statusParam.setStatus(state.ordinal());
		statusParam.setStatusInformation(buildStatusInformation(
				parameterName,
				state.ordinal(),
				statusInformation));
		statusParam.setCollectTime(collectTime);

		monitor.collectParameter(statusParam);
	}

	/**
	 * Build the status information text value
	 *
	 * @param parameterName The name of the parameter e.g. intrusionStatus, status
	 * @param ordinal       The numeric value of the status (0, 1, 2)
	 * @param value         The text value of the status information
	 * @return {@link String} value
	 */
	public static String buildStatusInformation(final String parameterName, final int ordinal, final String value) {
		return String.format("%s: %s (%s)", parameterName, ordinal, value);
	}

	/**
	 * Collect the energy usage based on the power consumption
	 *
	 * @param monitor          The monitor instance we wish to collect
	 * @param collectTime      The current collect time
	 * @param powerConsumption The power consumption value. Never null
	 * @param hostname         The system host name used for debug purpose
	 */
	static void collectEnergyUsageFromPower(final Monitor monitor, final Long collectTime, final Double powerConsumption, String hostname) {

		updateNumberParameter(
			monitor,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT,
			collectTime,
			powerConsumption,
			powerConsumption
		);

		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, POWER_CONSUMPTION_PARAMETER, true);

		final Double deltaTimeMs = CollectHelper.subtract(POWER_CONSUMPTION_PARAMETER,
				collectTime.doubleValue(), collectTimePrevious);

		// Convert deltaTimeMs from milliseconds (ms) to seconds
		final Double deltaTime = deltaTimeMs != null ? deltaTimeMs / 1000.0 : null;

		// Calculate energy usage from Power Consumption: E = P * T
		final Double energyUsage = CollectHelper.multiply(POWER_CONSUMPTION_PARAMETER,
				powerConsumption, deltaTime);

		if (energyUsage != null) {

			// The energy will start from the energy usage delta
			Double energy = energyUsage;

			// The previous value is needed to get the total energy in joules
			Double previousEnergy = CollectHelper.getNumberParamRawValue(monitor,
				ENERGY_PARAMETER, true);

			// Ok, we have the previous energy value ? sum the previous energy and the current delta energy usage
			if (previousEnergy != null) {
				energy +=  previousEnergy;
			}

			// Everything is good update the energy parameter in the HostMonitoring
			updateNumberParameter(
				monitor,
				ENERGY_PARAMETER,
				ENERGY_PARAMETER_UNIT,
				collectTime,
				energy,
				energy
			);

			// Update the energy usage delta parameter in the HostMonitoring
			updateNumberParameter(
				monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				energyUsage,
				energyUsage
			);

		} else {
			log.debug("Cannot compute energy usage for monitor {} on system {}. Current power consumption {}, current time {}, previous time {}",
					monitor.getId(), hostname, powerConsumption, collectTime, collectTimePrevious);
		}
	}

	/**
	 * Collect the power consumption based on the energy usage Power Consumption = Delta(energyUsageRaw) / Delta(CollectTime)
	 * @param monitor           The monitor instance we wish to collect
	 * @param collectTime       The current collect time
	 * @param energyRawKw       The cumulative energy value in kW. Never null
	 * @param hostname          The system host name used for debug purpose
	 */
	static void collectPowerFromEnergyUsage(final Monitor monitor, final Long collectTime, final Double energyRawKw, final String hostname) {

		// Update the raw value for energy usage
		updateNumberParameter(
			monitor,
			ENERGY_USAGE_PARAMETER,
			ENERGY_USAGE_PARAMETER_UNIT,
			collectTime,
			null,
			energyRawKw
		);

		// Previous raw value
		final Double energyRawKwPrevious = CollectHelper.getNumberParamRawValue(monitor, ENERGY_USAGE_PARAMETER, true);

		// Previous collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, ENERGY_USAGE_PARAMETER, true);

		// Calculate the delta to get the energy usage value
		final Double energyUsageKw = CollectHelper.subtract(ENERGY_USAGE_PARAMETER, energyRawKw, energyRawKwPrevious);

		// Calculate the delta time in milliseconds
		final Double deltaTimeMs = CollectHelper.subtract("energyUsage.collectTime", collectTime.doubleValue(), collectTimePrevious);

		// Convert delta time milliseconds to seconds
		final Double deltaTime = deltaTimeMs != null ? deltaTimeMs / 1000.0 : null;

		if (energyUsageKw != null) {
			updateNumberParameter(
				monitor,
				ENERGY_USAGE_PARAMETER,
				ENERGY_USAGE_PARAMETER_UNIT,
				collectTime,
				energyUsageKw * 1000 * 3600, // kW-hours to Joules
				energyRawKw
			);

		} else {
			log.debug("Cannot compute energy usage for monitor {} on system {}. Current raw energy {}, previous raw energy {}",
					monitor.getId(), hostname, energyRawKw, energyRawKwPrevious);
		}

		if (energyUsageKw != null && deltaTime != null && deltaTime != 0.0) {
			// Calculate the power consumption in watts corresponding to the energy usage
			double powerConsumptionWatts = energyUsageKw / deltaTime * 1000 * 3600;

			updateNumberParameter(
				monitor,
				POWER_CONSUMPTION_PARAMETER,
				POWER_CONSUMPTION_PARAMETER_UNIT,
				collectTime,
				powerConsumptionWatts,
				powerConsumptionWatts
			);
		} else {
			log.debug("Cannot compute power consumption for monitor {} on system {}. Current raw energy {}, previous raw energy {}, current time {}, previous time {}",
					monitor.getId(), hostname, energyRawKw, energyRawKwPrevious, collectTime, collectTimePrevious);
		}

		// Updating the monitor's energy parameter
		updateNumberParameter(
			monitor,
			ENERGY_PARAMETER,
			ENERGY_PARAMETER_UNIT,
			collectTime,
			energyRawKw * 3600 * 1000, // value
			energyRawKw // raw value
		);
	}

	/**
	 * Check if the given value is a valid positive
	 *
	 * @param value The {@link Double} value to check
	 * @return <code>true</code> if the value is not null and greater than equals 0
	 */
	static boolean isValidPositive(final Double value) {
		return value != null && value >= 0;
	}


	/**
	 * Check if the given percentage value is not null and greater than equals 0 and latest than equals 100
	 *
	 * @param percent The percentage value to check
	 * @return boolean value, <code>true</code> if the percentage is valid otherwise <code>false</code>
	 */
	static boolean isValidPercentage(final Double percent) {
		return percent != null
				&& percent >= 0
				&& percent <= 100;
	}
}
