package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.MONITOR_ATTRIBUTE_NAME;

import com.sentrysoftware.matrix.common.helpers.ArrayHelper;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.strategy.utils.CollectHelper;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.math.RoundingMode;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class HostMonitorThermalCalculator {

	private TelemetryManager telemetryManager;
	private static final String TEMPERATURE_METRIC = "hw.temperature";
	private static final String IS_CPU_SENSOR = "__is_cpu_sensor";
	private static final String HW_HOST_AVERAGE_CPU_TEMPERATURE = "__hw.host.average_cpu_temperature";
	private static final String HW_HOST_AMBIENT_TEMPERATURE = "__hw.host.ambient_temperature";
	private static final String TRUE_STRING = "true";

	/**
	 * Computes the heating margin of the host monitor
	 * @return Double
	 */
	public static Double computeHeatingMargin() {
		return null;
	}

	/**
	 * Compute temperature metrics for the current host monitor:
	 * <ul>
	 * <li><b>ambientTemperature</b>: the minimum temperature between 5 and 100 degrees Celsius</li>
	 * <li><b>cpuTemperature</b>: the average CPU temperatures</li>
	 * <li><b>cpuThermalDissipationRate</b>: the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit)</li>
	 * </ul>
	 */
	public void computeHostTemperatureMetrics() {
		final Map<String, Monitor> temperatureMonitors = telemetryManager
			.getMonitors()
			.get(KnownMonitorType.TEMPERATURE.getKey());
		// No temperatures then no computation
		if (temperatureMonitors == null || temperatureMonitors.isEmpty()) {
			log.debug(
				"Hostname {} - Could not compute temperature metrics (ambientTemperature, cpuTemperature, cpuThermalDissipationRate)",
				telemetryManager.getHostname()
			);
			return;
		}

		double ambientTemperature = 35.0;
		double cpuTemperatureAverage = 0;
		double cpuTemperatureCount = 0;

		// Loop over all the temperature monitors to compute the ambient temperature, cpuTemperatureCount and cpuTemperatureAverage
		for (final Monitor temperatureMonitor : temperatureMonitors.values()) {
			// Get the temperature value
			final Double temperatureValue = CollectHelper.getNumberMetricValue(temperatureMonitor, TEMPERATURE_METRIC, false);

			// If there is no temperature value, no need to continue process this monitor
			final NumberMetric temperatureMetric = temperatureMonitor.getMetric(TEMPERATURE_METRIC, NumberMetric.class);
			if (temperatureMetric == null || !temperatureMetric.isUpdated() || temperatureValue == null) {
				continue;
			}

			// Is this the ambient temperature? (which should be the lowest measured temperature... except if it's less than 5°)
			if (temperatureValue < ambientTemperature && temperatureValue > 5) {
				ambientTemperature = temperatureValue;
			}
			final Double warningThreshold = CollectHelper.getNumberMetricValue(
				temperatureMonitor,
				"hw.temperature.limit{limit_type=\"high.degraded\"}",
				false
			);

			// Get the isCpuSensor flag
			boolean isCpuSensor = false;
			if (!TRUE_STRING.equals(temperatureMonitor.getAttribute(IS_CPU_SENSOR))) {
				isCpuSensor =
					isCpuSensor(
						warningThreshold,
						temperatureMonitor.getAttribute(MONITOR_ATTRIBUTE_NAME),
						temperatureMonitor.getAttribute("info")
					);
				if (isCpuSensor) {
					temperatureMonitor.getAttributes().put(IS_CPU_SENSOR, TRUE_STRING);
				}
			} else {
				isCpuSensor = true;
			}

			// Is this a CPU sensor?
			if (isCpuSensor && temperatureValue > 5) {
				cpuTemperatureAverage += temperatureValue;
				cpuTemperatureCount++;
			}
		}

		// Find the host monitor
		final Monitor hostMonitor = telemetryManager.getHostMonitor();

		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());

		// Sets the host ambient temperature as the minimum of all temperature sensors
		if (ambientTemperature < 35) {
			// Update the metric
			metricFactory.collectNumberMetric(
				hostMonitor,
				HW_HOST_AMBIENT_TEMPERATURE,
				ambientTemperature,
				telemetryManager.getStrategyTime()
			);
		}

		// Sets the average CPU temperature (to estimate the heat dissipation of the processors)
		if (cpuTemperatureCount > 0) {
			// Compute the average
			cpuTemperatureAverage /= cpuTemperatureCount;

			cpuTemperatureAverage = NumberHelper.round(cpuTemperatureAverage, 2, RoundingMode.HALF_UP);

			metricFactory.collectNumberMetric(
				hostMonitor,
				HW_HOST_AVERAGE_CPU_TEMPERATURE,
				cpuTemperatureAverage,
				telemetryManager.getStrategyTime()
			);

			// Calculate the dissipation rate
			computeHostThermalDissipationRate(hostMonitor, ambientTemperature, cpuTemperatureAverage);
		}
	}

	/**
	 * Calculate the heat dissipation rate of the processors (as a fraction of the maximum heat/power they can emit).
	 *
	 * @param hostMonitor           The host monitor we wish to update its heat dissipation rate
	 * @param ambientTemperature    The ambient temperature of the host
	 * @param cpuTemperatureAverage The CPU average temperature previously computed based on the cpu sensor count
	 */
	void computeHostThermalDissipationRate(
		final Monitor hostMonitor,
		final double ambientTemperature,
		final double cpuTemperatureAverage
	) {
		// Get the average CPU temperature computed at the discovery level
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final double ambientToWarningDifference =
			CollectHelper.getNumberMetricValue(hostMonitor, HW_HOST_AVERAGE_CPU_TEMPERATURE, false) - ambientTemperature;

		// Avoid the arithmetic exception
		if (ambientToWarningDifference != 0.0) {
			double cpuThermalDissipationRate = (cpuTemperatureAverage - ambientTemperature) / ambientToWarningDifference;

			// Do we have a consistent fraction
			if (cpuThermalDissipationRate >= 0 && cpuThermalDissipationRate <= 1) {
				cpuThermalDissipationRate = NumberHelper.round(cpuThermalDissipationRate, 2, RoundingMode.HALF_UP);
				metricFactory.collectNumberMetric(
					hostMonitor,
					"__hw.host.cpu.thermal_dissipation_rate",
					cpuThermalDissipationRate,
					telemetryManager.getStrategyTime()
				);
			}
		}
	}

	/**
	 * Check if the given information match a CPU sensor
	 *
	 * @param warningThreshold The warning threshold previously computed
	 * @param data             The string data to check
	 * @return <code>true</code> the warning threshold is greater than 10 degrees and the data contains "cpu" or "proc" otherwise
	 *         <code>false</code>
	 */
	static boolean isCpuSensor(final Double warningThreshold, final String... data) {
		// CHECKSTYLE:OFF
		return (
			warningThreshold != null &&
			warningThreshold > 10 &&
			data != null &&
			ArrayHelper.anyMatchLowerCase(HostMonitorThermalCalculator::matchesCpuSensor, data)
		);
		// CHECKSTYLE:ON
	}

	/**
	 * Check whether the given value matches a CPU sensor
	 *
	 * @param value string value to check
	 * @return <code>true</code> if the given value matches "cpu" or "proc" otherwise <code>false</code>
	 */
	static boolean matchesCpuSensor(final String value) {
		return value.contains("cpu") || value.contains("proc");
	}

	/**
	 * Estimates the overall average temperature
	 * @return Double
	 */
	public static Double estimateAverageTemperature() {
		return null;
	}

	/**
	 * Estimates temperature warning threshold average
	 * @return Double
	 */
	public static Double estimateTemperatureWarningThresholdAverage() {
		return null;
	}

	/**
	 * Estimates thermal dissipation rate
	 * @return Double
	 */
	public static Double estimateThermalDissipationRate() {
		return null;
	}
}
