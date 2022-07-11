package com.sentrysoftware.matrix.engine.strategy.collect;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
import com.sentrysoftware.matrix.common.meta.parameter.state.PredictedFailure;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;

@Slf4j
public class CollectHelper {

	private CollectHelper() {}

	public static final Pattern VALUE_TABLE_PATTERN = Pattern.compile("^\\s*valuetable.column\\((\\d+)\\)\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final List<String> MAYBE_NEGATIVE_PARAMETERS;

	static {

		// Update this list when you collect a parameter accepting a negative value
		MAYBE_NEGATIVE_PARAMETERS = Collections.unmodifiableList(Collections.emptyList());
	}

	/**
	 * Translate the state String value to a {@link IState} Enum
	 *
	 * @param stateValue    value in String format to translate
	 * @param interpreter   interpret function used to translate the state
	 * @param parameterName The name of the parameter we wish to collect
	 * @param monitorId     Current collected monitor identifier
	 * @param hostname      Current hostname used for logging only
	 * @return {@link IState} value
	 */
	public static IState translateState(final String stateValue, 
			@NonNull final Function<String, Optional<? extends IState>> interpreter,
			@NonNull final String parameterName, @NonNull final String monitorId,
			@NonNull final String hostname) {

		if (stateValue == null) {
			return null;
		}

		final Optional<? extends IState> state = interpreter.apply(stateValue);
		if (state.isEmpty()) {
			log.error("Hostname {} - Unexpected state value for instance {}. {} is null.",
					hostname,
					monitorId,
					parameterName
			);
			return null;
		}

		return state.get();
	}

	/**
	 * From the given row extract the value corresponding to the given <code>valueTableColumn</code>
	 *
	 * @param valueTable       The unique key of the {@link Source} used for debug purpose
	 * @param parameterKey     The unique key of the parameter. E.g. status, statusInformation, intrusionStatus...
	 * @param monitorType      The type of the monitor we wish to collect
	 * @param row              The data which indicate the parameters to collect
	 * @param valueTableColumn The column index formatted as `ValueTable.Column($number)`
	 * @param hostname         Current hostname used for logging only
	 * @return {@link String} value
	 */
	public static String getValueTableColumnValue(@NonNull final String valueTable, @NonNull final String parameterKey,
			@NonNull final MonitorType monitorType, @NonNull final List<String> row,
			final String valueTableColumn, @NonNull final String hostname) {

		if (valueTableColumn == null) {
			return null;
		}

		final Matcher matcher = VALUE_TABLE_PATTERN.matcher(valueTableColumn);

		if (matcher.find()) {

			final int columnIndex = Integer.parseInt(matcher.group(1)) - 1;

			if (columnIndex >= 0 && columnIndex < row.size()) {

				return row.get(columnIndex);

			} else {
				log.warn("Hostname {} - Collect - Column number {} is invalid for the value table source {}. Column number should not exceed the size of the row. ParameterKey {} - Row {} - MonitorType {}.",
						hostname,
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
	 * Get the {@link DiscreteParam} state value
	 *
	 * @param monitor       The {@link Monitor} instance we wish to extract the {@link DiscreteParam} state
	 * @param parameterName The name of the {@link DiscreteParam} instance
	 * @return a {@link IState} value (OK, WARN or ALARM)
	 */
	public static IState getParameterState(final Monitor monitor, final String parameterName) {

		final DiscreteParam parameter = monitor.getParameter(parameterName, DiscreteParam.class);

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
	 * @param number	The {@link Number} whose {@link Double} value should be extracted from
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
	 * @param hostname      Current hostname used for logging only
	 *
	 * @return {@link Double} value
	 */
	public static Double subtract(final String parameterName, final Double minuend, final Double subtrahend, final String hostname) {

		if (minuend == null || subtrahend == null) {
			return null;
		}

		final double result = minuend - subtrahend;

		if (result < 0 && !MAYBE_NEGATIVE_PARAMETERS.contains(parameterName)) {
			log.warn("Hostname {} - Suspicious negative value ({} - {}) = {} for parameter {}.", 
					hostname, minuend, subtrahend, result, parameterName);
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
	 * @param hostname  Current hostname used for logging only
	 * @return {@link Double} value
	 */
	public static Double divide(final String parameter, final Double dividend, final Double divisor, final String hostname) {

		if (dividend == null || divisor == null) {
			return null;
		}

		if (divisor == 0) {
			log.debug("Hostname {} - Couldn't compute ({} / {}) for parameter {}. Division by zero is not allowed.", hostname, dividend, divisor, parameter);
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
	 * @param hostname            Current hostname used for logging only
	 *
	 * @return {@link Double} value
	 */
	public static Double rate(String parameterName, Double value, Double previousValue, Double collectTime,
			Double previousCollectTime, String hostname) {
		return CollectHelper.divide(parameterName,
				CollectHelper.subtract(parameterName, value, previousValue, hostname),
				CollectHelper.subtract(parameterName, collectTime, previousCollectTime, hostname), hostname);
	}

	/**
	 * Computes rate for the given parameter.
	 *
	 * @param parameterName						The name of the parameter.
	 * @param currentValue						The current value of the parameter
	 * @param currentCollectTimeInMilliseconds	The time when the current value of the parameter was collected,
	 *                                          in milliseconds.
	 * @param monitor							The {@link Monitor} having the parameter.
	 * @param hostname                          Current hostname used for logging only
	 *
	 * @return									The value of the parameter's rate.<br>
	 * 											Null if the computation could not be done.
	 */
	public static Double rate(String parameterName, Double currentValue,
							  Long currentCollectTimeInMilliseconds, Monitor monitor, String hostname) {

		if (parameterName == null || currentValue == null || currentCollectTimeInMilliseconds == null
			|| monitor == null) {

			return null;
		}

		// Getting the previous value
		Double previousValue = CollectHelper.getNumberParamRawValue(monitor, parameterName, true);
		if (previousValue == null) {
			return null;
		}

		// Getting the previous collect time
		final Double previousCollectTimeInMilliseconds = CollectHelper.getNumberParamCollectTime(monitor, parameterName,
			true);
		if (previousCollectTimeInMilliseconds == null) {

			// This should never happen
			log.warn("Hostname {} - Found previous {} value, but could not find previous collect time.",
				hostname, parameterName);

			return null;
		}

		// Converting the collect times from milliseconds to seconds, and computing the rate
		return rate(parameterName, currentValue, previousValue,
			currentCollectTimeInMilliseconds.doubleValue() / 1000.0, previousCollectTimeInMilliseconds / 1000.0, hostname);
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
	 * Update the status information text parameter value in the given {@link Monitor} instance
	 *
	 * @param monitor           The monitor we wish to collect the status information parameter value
	 * @param collectTime       The collect time for this parameter
	 * @param statusInformation The status information
	 * @param status            The Status to use in case statusInformation is empty or <code>null</code>
	 */
	public static void updateStatusInformation(@NonNull final Monitor monitor,
			@NonNull final Long collectTime, String statusInformation, final Status status) {

		// No status information? statusInformation becomes the status display name
		if ((statusInformation == null || statusInformation.isBlank()) && status != null) {
			statusInformation = status.getDisplayName();
		}

		// No status information? the parameter cannot be collected
		if (statusInformation == null) {
			return;
		}

		TextParam statusInformationParam = monitor.getParameter(STATUS_INFORMATION_PARAMETER, TextParam.class);

		if (statusInformationParam == null) {
			statusInformationParam = TextParam
					.builder()
					.name(STATUS_INFORMATION_PARAMETER)
					.build();
		}

		statusInformationParam.setValue(statusInformation);
		statusInformationParam.setCollectTime(collectTime);

		monitor.collectParameter(statusInformationParam);
	}

	/**
	 * Update the discrete parameter {@link IState} value in the given {@link Monitor} instance
	 * 
	 * @param monitor       The monitor we wish to collect the discrete parameter state value
	 * @param parameterName The name of the parameter we wish to collect
	 * @param collectTime   The collect strategy time
	 * @param state         The {@link IState} instance. E.g {@link Status},
	 *                      {@link PredictedFailure} or {@link PowerState}
	 */
	public static void updateDiscreteParameter(final Monitor monitor, final String parameterName,
			final Long collectTime, final IState state) {

		DiscreteParam discrecteParam = monitor.getParameter(parameterName, DiscreteParam.class);

		if (discrecteParam == null) {
			discrecteParam = DiscreteParam
					.builder()
					.name(parameterName)
					.state(state)
					.collectTime(collectTime)
					.build();
		} else {
			discrecteParam.setState(state);
			discrecteParam.setCollectTime(collectTime);
		}


		monitor.collectParameter(discrecteParam);
	}

	/**
	 * Collect the energy usage based on the power consumption
	 *
	 * @param monitor          The monitor instance we wish to collect
	 * @param collectTime      The current collect time
	 * @param powerConsumption The power consumption value. Never null
	 * @param hostname         The system host name used for debug purpose
	 */
	public static void collectEnergyUsageFromPower(final Monitor monitor, final Long collectTime, final Double powerConsumption, String hostname) {

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
				collectTime.doubleValue(), collectTimePrevious, hostname);

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
			log.debug("Hostname {} - Cannot compute energy usage for monitor {}. Current power consumption {} - Current time {} - Previous time {}.",
					hostname, monitor.getId(), powerConsumption, collectTime, collectTimePrevious);
		}
	}

	/**
	 * Collect the power consumption based on the energy usage Power Consumption = Delta(energyUsageRaw) / Delta(CollectTime)
	 * @param monitor           The monitor instance we wish to collect
	 * @param collectTime       The current collect time
	 * @param energyRawKw       The cumulative energy value in kW. Never null
	 * @param hostname          The system host name used for debug purpose
	 */
	public static void collectPowerFromEnergyUsage(final Monitor monitor, final Long collectTime, @NonNull final Double energyRawKw, final String hostname) {

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
		final Double energyUsageKw = CollectHelper.subtract(ENERGY_USAGE_PARAMETER, energyRawKw, energyRawKwPrevious, hostname);

		// Calculate the delta time in milliseconds
		final Double deltaTimeMs = CollectHelper.subtract("energyUsage.collectTime", collectTime.doubleValue(), collectTimePrevious, hostname);

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
			log.debug("Hostname {} - Could not compute energy usage for monitor {}. Current raw energy: {} - Previous raw energy: {}.",
					hostname, monitor.getId(), energyRawKw, energyRawKwPrevious);
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
			log.debug("Hostname {} - Cannot compute power consumption for monitor {}. Current raw energy {} - Previous raw energy {} - Current time {} - Previous time {}.",
					hostname, monitor.getId(), energyRawKw, energyRawKwPrevious, collectTime, collectTimePrevious);
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
	public static boolean isValidPercentage(final Double percent) {
		return percent != null
				&& percent >= 0
				&& percent <= 100;
	}

}
