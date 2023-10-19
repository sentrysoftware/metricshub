package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AMBIENT_TEMPERATURE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AVERAGE_CPU_TEMPERATURE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TEMPERATURE_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HostMonitorThermalCalculatorTest {

	private HostMonitorThermalCalculator hostMonitorThermalCalculator;
	private TelemetryManager telemetryManager;
	private static final String TEMPERATURE_MONITOR_ID = "temperatureMonitor";
	private static final String HOST_ID = "host";
	private static final String IS_CPU_SENSOR = "__is_cpu_sensor";
	private static final String HW_HOST_CPU_THERMAL_DISSIPATION_RATE = "__hw.host.cpu.thermal_dissipation_rate";
	private static final String TRUE_STRING = "true";
	private static final String CPU_ID = "cpu";
	private static final String DISK_CONTROLLER_ID = "disk_controller";
	private static final String TEMPERATURE = "hw.temperature";
	private static final String TEMPERATURE_WARNING_THRESHOLD = "hw.temperature.limit{limit_type=\"high.degraded\"}";
	private static final String HW_HOST_HEATING_MARGIN = "hw.host.heating_margin";

	@BeforeEach
	void setup() {
		telemetryManager =
			TelemetryManager
				.builder()
				.strategyTime(100L)
				.hostConfiguration(HostConfiguration.builder().hostId(HOST_ID).hostname(HOST_ID).build())
				.build();
		hostMonitorThermalCalculator = new HostMonitorThermalCalculator(telemetryManager);
	}

	@Test
	void testRunWithComputeTemperatureMetrics() {
		final Monitor host = Monitor.builder().id(HOST_ID).type(KnownMonitorType.HOST.getKey()).build();

		// Set host as endpoint
		host.setAsEndpoint();

		// temperatureMonitors is null

		hostMonitorThermalCalculator.computeHostTemperatureMetrics();
		assertNull(host.getMetric(TEMPERATURE_METRIC, NumberMetric.class));

		// temperatureMonitors is empty

		final Map<String, Map<String, Monitor>> monitors = telemetryManager.getMonitors();
		monitors.put(KnownMonitorType.TEMPERATURE.getKey(), new LinkedHashMap<>());

		hostMonitorThermalCalculator.computeHostTemperatureMetrics();
		assertNull(host.getMetric(TEMPERATURE_METRIC, NumberMetric.class));

		// No CPU sensor

		final Monitor temperatureMonitor = Monitor
			.builder()
			.id(TEMPERATURE_MONITOR_ID)
			.type(KnownMonitorType.TEMPERATURE.getKey())
			.build();

		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), HOST_ID);
		telemetryManager.addNewMonitor(temperatureMonitor, KnownMonitorType.TEMPERATURE.getKey(), TEMPERATURE_MONITOR_ID);

		assertNull(host.getMetric(HW_HOST_AMBIENT_TEMPERATURE, NumberMetric.class));
		assertNull(host.getMetric(HW_HOST_AVERAGE_CPU_TEMPERATURE, NumberMetric.class));

		final MetricFactory metricFactory = new MetricFactory(HOST_ID);
		metricFactory.collectNumberMetric(temperatureMonitor, TEMPERATURE_METRIC, 10.0, telemetryManager.getStrategyTime());
		hostMonitorThermalCalculator.computeHostTemperatureMetrics();

		assertEquals(10.0, host.getMetric(HW_HOST_AMBIENT_TEMPERATURE, NumberMetric.class).getValue());
		assertNull(host.getMetric(HW_HOST_AVERAGE_CPU_TEMPERATURE, NumberMetric.class));

		// Present CPU sensor
		host.setMetrics(new HashMap<>());
		temperatureMonitor.getAttributes().put(IS_CPU_SENSOR, TRUE_STRING);

		metricFactory.collectNumberMetric(temperatureMonitor, TEMPERATURE_METRIC, 10.0, telemetryManager.getStrategyTime());

		telemetryManager.setMonitors(new LinkedHashMap<>());
		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), HOST_ID);
		telemetryManager.addNewMonitor(temperatureMonitor, KnownMonitorType.TEMPERATURE.getKey(), TEMPERATURE_MONITOR_ID);

		assertNull(host.getMetric(HW_HOST_AMBIENT_TEMPERATURE, NumberMetric.class));
		assertNull(host.getMetric(HW_HOST_AVERAGE_CPU_TEMPERATURE, NumberMetric.class));

		hostMonitorThermalCalculator.computeHostTemperatureMetrics();
		assertEquals(10.0, host.getMetric(HW_HOST_AMBIENT_TEMPERATURE, NumberMetric.class).getValue());
		assertEquals(10.0, host.getMetric(HW_HOST_AVERAGE_CPU_TEMPERATURE, NumberMetric.class).getValue());
		assertNull(host.getMetric(HW_HOST_CPU_THERMAL_DISSIPATION_RATE, NumberMetric.class));

		// Add another temperature monitor
		final Monitor otherTemperatureMonitor = Monitor
			.builder()
			.id(TEMPERATURE_MONITOR_ID)
			.type(KnownMonitorType.TEMPERATURE.getKey())
			.build();

		otherTemperatureMonitor.getAttributes().put(IS_CPU_SENSOR, TRUE_STRING);

		metricFactory.collectNumberMetric(
			otherTemperatureMonitor,
			TEMPERATURE_METRIC,
			20.0,
			telemetryManager.getStrategyTime()
		);
		telemetryManager.addNewMonitor(
			otherTemperatureMonitor,
			KnownMonitorType.TEMPERATURE.getKey(),
			TEMPERATURE_MONITOR_ID + "_2"
		);

		// Check the estimation of the host thermal dissipation rate
		hostMonitorThermalCalculator.computeHostTemperatureMetrics();
		assertEquals(1.0, host.getMetric(HW_HOST_CPU_THERMAL_DISSIPATION_RATE, NumberMetric.class).getValue());
	}

	@Test
	void testComputeHeatingMargin() {
		final Long strategyTime = 1696597422644L;
		telemetryManager.setStrategyTime(strategyTime);
		final Monitor host = Monitor.builder().id(HOST_ID).type(KnownMonitorType.HOST.getKey()).build();
		final Monitor cpu = Monitor
			.builder()
			.id(CPU_ID)
			.type(KnownMonitorType.CPU.getKey())
			.metrics(
				Map.of(
					TEMPERATURE,
					NumberMetric.builder().collectTime(strategyTime).name(TEMPERATURE).value(40D).build(),
					TEMPERATURE_WARNING_THRESHOLD,
					NumberMetric
						.builder()
						.collectTime(strategyTime)
						.name(TEMPERATURE_WARNING_THRESHOLD)
						.value(41D) // We want a low heating margin to make sure it's not used
						.build()
				)
			)
			.build();

		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), HOST_ID);
		telemetryManager.addNewMonitor(cpu, KnownMonitorType.CPU.getKey(), CPU_ID);

		// There shouldn't be a heating margin yet
		assertNull(host.getMetric(HW_HOST_HEATING_MARGIN, NumberMetric.class));

		// With only the CPU there shouldn't be any heating margin
		hostMonitorThermalCalculator.computeHeatingMargin();
		assertNull(host.getMetric(HW_HOST_HEATING_MARGIN, NumberMetric.class));

		// Let's add a disk controller to be able to calculate some heating margin
		final Monitor diskController = Monitor
			.builder()
			.id(DISK_CONTROLLER_ID)
			.type(KnownMonitorType.DISK_CONTROLLER.getKey())
			.metrics(
				Map.of(
					TEMPERATURE,
					NumberMetric.builder().collectTime(strategyTime).name(TEMPERATURE).value(10D).build(),
					TEMPERATURE_WARNING_THRESHOLD,
					NumberMetric
						.builder()
						.collectTime(strategyTime)
						.name(TEMPERATURE_WARNING_THRESHOLD)
						.value(30D) // Heating margin of 20 degrees
						.build()
				)
			)
			.build();
		telemetryManager.addNewMonitor(diskController, KnownMonitorType.DISK_CONTROLLER.getKey(), DISK_CONTROLLER_ID);

		hostMonitorThermalCalculator.computeHeatingMargin();
		assertNotNull(host.getMetric(HW_HOST_HEATING_MARGIN, NumberMetric.class));
		assertEquals(20D, host.getMetric(HW_HOST_HEATING_MARGIN, NumberMetric.class).getValue());
	}
}
