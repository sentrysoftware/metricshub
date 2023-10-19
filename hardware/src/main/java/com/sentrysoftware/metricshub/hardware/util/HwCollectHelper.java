package com.sentrysoftware.metricshub.hardware.util;

import com.sentrysoftware.metricshub.engine.strategy.utils.CollectHelper;
import com.sentrysoftware.metricshub.engine.strategy.utils.MathOperationsHelper;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HwCollectHelper {

	/**
	 * Check if the given value is a valid positive
	 *
	 * @param value The {@link Double} value to check
	 * @return <code>true</code> if the value is not null and greater than equals 0
	 */
	public static boolean isValidPositive(final Double value) {
		return value != null && value >= 0;
	}

	/**
	 * Check if the given ratio value is not null and greater than or equals 0 and less than or equals 1
	 *
	 * @param ratio The ratio value to check
	 * @return boolean value, <code>true</code> if the ratio is valid otherwise <code>false</code>
	 */
	public static boolean isValidRatio(final Double ratio) {
		return ratio != null && ratio >= 0 && ratio <= 1;
	}

	/**
	 * Estimates energy consumption using power consumption estimated value
	 * @param monitor the monitor to collect
	 * @param telemetryManager the telemetry manager
	 * @param estimatedPower the previously estimated power consumption
	 * @param powerMetricName the metricName to estimate e.g: hw.power{hw.type="fan"} or hw.power{hw.type="fan"}
	 * @param energyMetricName the metricName to estimate e.g: hw.energy{hw.type="fan"} or hw.energy{hw.type="fan"}
	 * @param collectTime the current collect time in milliseconds
	 * @return estimated Double energy value
	 */
	public static Double estimateEnergyUsingPower(
		final Monitor monitor,
		final TelemetryManager telemetryManager,
		final Double estimatedPower,
		final String powerMetricName,
		final String energyMetricName,
		final Long collectTime
	) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		final Double collectTimePrevious = CollectHelper.getNumberMetricCollectTime(monitor, powerMetricName, true);

		final Double deltaTimeMs = MathOperationsHelper.subtract(
			powerMetricName,
			Double.valueOf(collectTime),
			collectTimePrevious,
			hostname
		);

		// Convert deltaTimeMs from milliseconds (ms) to seconds
		final Double deltaTime = deltaTimeMs != null ? deltaTimeMs / 1000.0 : null;

		// Calculate the usage over time. e.g from Power Consumption: E = P * T
		final Double usageDelta = MathOperationsHelper.multiply(powerMetricName, estimatedPower, deltaTime, hostname);

		if (usageDelta != null) {
			// The counter will start from the usage delta
			Double counter = usageDelta;

			// The previous counter is needed to make a sum with the delta counter value on this collect
			final Double previousCounter = CollectHelper.getNumberMetricValue(monitor, energyMetricName, true);

			// Ok, we have the previous counter value ? sum the previous counter and the current delta counter
			if (previousCounter != null) {
				counter += previousCounter;
			}

			// Everything is good return the counter metric
			return counter;
		} else {
			log.debug(
				"Hostname {} - Cannot calculate energy {} for monitor {}. Current raw value {} - Current time {} - Previous time {}.",
				hostname,
				energyMetricName,
				monitor.getId(),
				estimatedPower,
				collectTime,
				collectTimePrevious
			);
		}
		return null;
	}

	/**
	 * Calculate a rate for the given metric between the current collect and the previous collect
	 * @param monitor           The monitor from which to retrieve the metric value
	 * @param counterMetricName The name of the counter metric we want to calculate the rate from
	 * @param rateMetricName    The name of the rate metric we are caculating
	 * @param hostname          The hostname
	 * @return the calculated rate
	 */
	public static Double calculateMetricRate(
		final Monitor monitor,
		final String counterMetricName,
		final String rateMetricName,
		final String hostname
	) {
		final Double value = CollectHelper.getNumberMetricValue(monitor, counterMetricName, false);
		final Double previousValue = CollectHelper.getNumberMetricValue(monitor, counterMetricName, true);
		final Double collectTime = CollectHelper.getNumberMetricCollectTime(monitor, counterMetricName, false);
		final Double previousCollectTime = CollectHelper.getNumberMetricCollectTime(monitor, counterMetricName, true);

		return MathOperationsHelper.rate(rateMetricName, value, previousValue, collectTime, previousCollectTime, hostname);
	}

	/**
	 * Generates the corresponding power metric name base on monitor type
	 * @param monitorType the type of the monitor
	 * @return power metric's name  e.g: hw.power{hw.type="network"} (General format is: hw.power{hw.type="<type>"})
	 */
	public static String generatePowerMetricNameForMonitorType(final String monitorType) {
		return "hw.power{hw.type=\"" + monitorType + "\"}";
	}

	/**
	 * Generates the corresponding energy metric name base on monitor type
	 * @param monitorType the type of the monitor
	 * @return energy metric's name  e.g: hw.energy{hw.type="network"} (General format is: hw.energy{hw.type="<type>"})
	 */
	public static String generateEnergyMetricNameForMonitorType(final String monitorType) {
		return "hw.energy{hw.type=\"" + monitorType + "\"}";
	}

	/**
	 * Check if at least one monitor in the given map collects the power consumption or the energy
	 *
	 * @param monitors map of monitors
	 * @return boolean value
	 */
	public static boolean isPowerCollected(final Map<String, Monitor> monitors) {
		return Optional
			.ofNullable(monitors)
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.anyMatch(monitor ->
				CollectHelper.getUpdatedNumberMetricValue(monitor, generatePowerMetricNameForMonitorType(monitor.getType())) !=
				null ||
				CollectHelper.getUpdatedNumberMetricValue(monitor, generateEnergyMetricNameForMonitorType(monitor.getType())) !=
				null
			);
	}
}
