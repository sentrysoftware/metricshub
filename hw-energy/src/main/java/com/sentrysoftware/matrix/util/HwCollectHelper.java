package com.sentrysoftware.matrix.util;

import com.sentrysoftware.matrix.strategy.utils.MathOperationsHelper;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
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

		final Double collectTimePrevious = com.sentrysoftware.matrix.strategy.utils.CollectHelper.getNumberMetricCollectTime(
			monitor,
			powerMetricName,
			true
		);

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
			final Double previousCounter = com.sentrysoftware.matrix.strategy.utils.CollectHelper.getNumberMetricValue(
				monitor,
				energyMetricName,
				true
			);

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
}
