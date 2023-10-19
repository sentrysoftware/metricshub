package com.sentrysoftware.metricshub.hardware;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.DISK_CONTROLLER_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.DISK_CONTROLLER_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_SPEED_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_CPU_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AMBIENT_TEMPERATURE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AVERAGE_CPU_TEMPERATURE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_ENERGY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_MEMORY_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_PHYSICAL_DISK_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.LOCALHOST;
import static com.sentrysoftware.metricshub.hardware.common.Constants.MEMORY_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.MEMORY_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.NETWORK_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.NETWORK_LINK_SPEED_ATTRIBUTE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.NETWORK_LINK_STATUS_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.NETWORK_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.NETWORK_TRANSMITTED_BANDWIDTH_UTILIZATION_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.PHYSICAL_DISK_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.PHYSICAL_DISK_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.ROBOTICS_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.ROBOTICS_MOVE_COUNT_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.ROBOTICS_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TAPE_DRIVE_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TAPE_DRIVE_MOUNT_COUNT_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TAPE_DRIVE_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TAPE_DRIVE_UNMOUNT_COUNT_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.TEMPERATURE_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.PowerMeter;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HardwareEnergyPostExecutionServiceTest {

	private static final Long STRATEGY_TIME = 1696597422644L;
	private static final Long NEXT_STRATEGY_TIME = STRATEGY_TIME + 2 * 60 * 1000;

	private HardwareEnergyPostExecutionService hardwareEnergyPostExecutionService;

	private TelemetryManager telemetryManager = null;

	private static final String DISK_CONTROLLER = KnownMonitorType.DISK_CONTROLLER.getKey();
	private static final String FAN = KnownMonitorType.FAN.getKey();
	private static final String MEMORY = KnownMonitorType.MEMORY.getKey();
	private static final String ROBOTICS = KnownMonitorType.ROBOTICS.getKey();
	private static final String TAPE_DRIVE = KnownMonitorType.TAPE_DRIVE.getKey();
	private static final String TEMPERATURE = KnownMonitorType.TEMPERATURE.getKey();
	private static final String HOST = KnownMonitorType.HOST.getKey();
	private static final String PHYSICAL_DISK = KnownMonitorType.PHYSICAL_DISK.getKey();
	private static final String NETWORK = KnownMonitorType.NETWORK.getKey();

	@BeforeEach
	void init() {
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).build())
				.strategyTime(STRATEGY_TIME)
				.build();
	}

	@Test
	void testRunWithFanMonitor() {
		// Create a fan monitor
		final Monitor fanMonitor = Monitor
			.builder()
			.type(FAN)
			.metrics(new HashMap<>(Map.of(FAN_SPEED_METRIC, NumberMetric.builder().value(0.7).build())))
			.build();

		// Set the previously created fan monitor in telemetryManager
		final Map<String, Monitor> fanMonitors = new HashMap<>(Map.of("monitor1", fanMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(FAN, fanMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = fanMonitor.getMetric(FAN_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(fanMonitor.getMetric(FAN_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(fanMonitor.getMetric(FAN_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithRoboticsMonitor() {
		// Create a robotics monitor
		final Monitor roboticsMonitor = Monitor
			.builder()
			.type(ROBOTICS)
			.metrics(new HashMap<>(Map.of(ROBOTICS_MOVE_COUNT_METRIC, NumberMetric.builder().value(0.7).build())))
			.build();

		// Set the previously created robotics monitor in telemetryManager
		final Map<String, Monitor> roboticsMonitors = new HashMap<>(Map.of("monitor2", roboticsMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(ROBOTICS, roboticsMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = roboticsMonitor.getMetric(ROBOTICS_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(roboticsMonitor.getMetric(ROBOTICS_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(roboticsMonitor.getMetric(ROBOTICS_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithTapeDriveMonitor() {
		// Create a tape drive monitor
		final Monitor tapeDriveMonitor = Monitor
			.builder()
			.type(TAPE_DRIVE)
			.metrics(
				new HashMap<>(
					Map.of(
						TAPE_DRIVE_MOUNT_COUNT_METRIC,
						NumberMetric.builder().value(0.7).build(),
						TAPE_DRIVE_UNMOUNT_COUNT_METRIC,
						NumberMetric.builder().value(0.1).build()
					)
				)
			)
			.attributes(new HashMap<>(Map.of("name", "lto123")))
			.build();

		// Set the previously created tape drive monitor in telemetryManager
		final Map<String, Monitor> tapeDriveMonitors = new HashMap<>(Map.of("monitor3", tapeDriveMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(TAPE_DRIVE, tapeDriveMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = tapeDriveMonitor.getMetric(TAPE_DRIVE_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(tapeDriveMonitor.getMetric(TAPE_DRIVE_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(tapeDriveMonitor.getMetric(TAPE_DRIVE_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithDiskControllerMonitor() {
		// Create a disk controller monitor
		final Monitor diskControllerMonitor = Monitor.builder().type(DISK_CONTROLLER).build();

		// Set the previously created disk controller monitor in telemetryManager
		final Map<String, Monitor> diskControllerMonitors = new HashMap<>(Map.of("monitor4", diskControllerMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(DISK_CONTROLLER, diskControllerMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = diskControllerMonitor.getMetric(DISK_CONTROLLER_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(diskControllerMonitor.getMetric(DISK_CONTROLLER_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(diskControllerMonitor.getMetric(DISK_CONTROLLER_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithMemoryMonitor() {
		// Create a fan monitor
		final Monitor memoryMonitor = Monitor.builder().type(MEMORY).build();

		// Set the previously created monitor in telemetryManager
		final Map<String, Monitor> monitors = new HashMap<>(Map.of("monitor1", memoryMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(MEMORY, monitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = memoryMonitor.getMetric(MEMORY_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(memoryMonitor.getMetric(MEMORY_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(memoryMonitor.getMetric(MEMORY_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithPhysicalDiskMonitor() {
		// Create a physical disk monitor
		final Monitor physicalDiskMonitor = Monitor.builder().type(PHYSICAL_DISK).build();

		// Set the previously created physical disk monitor in telemetryManager
		final Map<String, Monitor> physicalDiskMonitors = new HashMap<>(Map.of("monitor5", physicalDiskMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(PHYSICAL_DISK, physicalDiskMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = physicalDiskMonitor.getMetric(PHYSICAL_DISK_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(physicalDiskMonitor.getMetric(PHYSICAL_DISK_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(physicalDiskMonitor.getMetric(PHYSICAL_DISK_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testRunWithNetworkMonitor() {
		// Create a network monitor
		final Monitor networkMonitor = Monitor
			.builder()
			.type(NETWORK)
			.attributes(new HashMap<>(Map.of("name", "real_network_card", NETWORK_LINK_SPEED_ATTRIBUTE, "100.0")))
			.metrics(
				new HashMap<>(
					Map.of(
						NETWORK_LINK_STATUS_METRIC,
						NumberMetric.builder().value(1.0).build(),
						NETWORK_TRANSMITTED_BANDWIDTH_UTILIZATION_METRIC,
						NumberMetric.builder().value(10.0).build()
					)
				)
			)
			.build();

		// Set the previously created network monitor in telemetryManager
		final Map<String, Monitor> networkMonitors = new HashMap<>(Map.of("monitor2", networkMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(NETWORK, networkMonitors)));
		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		final NumberMetric power = networkMonitor.getMetric(NETWORK_POWER_METRIC, NumberMetric.class);
		assertNotNull(power);

		// Check the computed and collected energy metric
		assertNull(networkMonitor.getMetric(NETWORK_ENERGY_METRIC, NumberMetric.class));

		// Next collect
		telemetryManager.setStrategyTime(NEXT_STRATEGY_TIME);
		power.save();
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected energy metric
		assertNotNull(networkMonitor.getMetric(NETWORK_ENERGY_METRIC, NumberMetric.class));
	}

	@Test
	void testComputeHostTemperatureMetrics() {
		// Create a host monitor
		final Monitor hostMonitor = Monitor.builder().type(HOST).build();

		// Set the host as endpoint
		hostMonitor.setAsEndpoint();

		// Create a temperature monitor
		final Monitor temperatureMonitor = Monitor.builder().type(TEMPERATURE).build();

		// Set the previously created monitor in telemetryManager
		telemetryManager.addNewMonitor(hostMonitor, KnownMonitorType.HOST.getKey(), "monitor0");
		telemetryManager.addNewMonitor(temperatureMonitor, KnownMonitorType.TEMPERATURE.getKey(), "monitor1");

		// Check the computed and collected metrics
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(temperatureMonitor, TEMPERATURE_METRIC, 10.0, telemetryManager.getStrategyTime());

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		//  Check the computed and collected temperature metrics (the host is not a cpu sensor)
		assertNotNull(hostMonitor.getMetric(HW_HOST_AMBIENT_TEMPERATURE, NumberMetric.class));
		assertNull(hostMonitor.getMetric(HW_HOST_AVERAGE_CPU_TEMPERATURE, NumberMetric.class));
	}

	@Test
	void testComputeHostPowerAndEnergyMetrics() {
		// Initialize the telemetry manager
		telemetryManager =
			TelemetryManager
				.builder()
				.strategyTime(new Date().getTime())
				.hostConfiguration(HostConfiguration.builder().hostId("localhost").build())
				.build();

		// We will compute estimated power. So, PowerMeter is set to estimated
		telemetryManager.setPowerMeter(PowerMeter.ESTIMATED);

		// Create the monitors: host and other monitors

		final Monitor host = Monitor
			.builder()
			.id(KnownMonitorType.HOST.getKey())
			.type(KnownMonitorType.HOST.getKey())
			.build();
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric previousPowerValue = metricFactory.collectNumberMetric(
			host,
			HW_HOST_POWER,
			60.0,
			telemetryManager.getStrategyTime() - 120 * 1000
		);
		previousPowerValue.save();

		host.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();
		enclosure.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();

		cpu.addMetric(
			HW_CPU_POWER,
			NumberMetric.builder().value(60.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		cpu.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		memory.addMetric(
			HW_MEMORY_POWER,
			NumberMetric.builder().value(4.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		memory.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		disk.addMetric(
			HW_PHYSICAL_DISK_POWER,
			NumberMetric.builder().value(6.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		disk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		diskNoPower.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		missingDisk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), KnownMonitorType.HOST.getKey());
		telemetryManager.addNewMonitor(cpu, KnownMonitorType.CPU.getKey(), "cpu1");
		telemetryManager.addNewMonitor(disk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_1");
		telemetryManager.addNewMonitor(memory, KnownMonitorType.MEMORY.getKey(), "memory1");
		telemetryManager.addNewMonitor(diskNoPower, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_noPower");
		telemetryManager.addNewMonitor(missingDisk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_2");
		telemetryManager.addNewMonitor(enclosure, KnownMonitorType.ENCLOSURE.getKey(), KnownMonitorType.ENCLOSURE.getKey());

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		//  Check the computed and collected temperature metrics (the host is not a cpu sensor)
		assertNotNull(host.getMetric(HW_HOST_POWER, NumberMetric.class));
		assertEquals(107.78, host.getMetric(HW_HOST_POWER, NumberMetric.class).getValue());
		assertNotNull(host.getMetric(HW_HOST_ENERGY, NumberMetric.class));
		assertEquals(12933.6, host.getMetric(HW_HOST_ENERGY, NumberMetric.class).getValue());
	}
}
