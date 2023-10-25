package com.sentrysoftware.metricshub.hardware;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.strategy.utils.CollectHelper;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import com.sentrysoftware.metricshub.engine.telemetry.metric.StateSetMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.DISK_CONTROLLER_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.DISK_CONTROLLER_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.FAN_SPEED_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HOST_1;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_CPU_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AMBIENT_TEMPERATURE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_AVERAGE_CPU_TEMPERATURE;
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
import static com.sentrysoftware.metricshub.hardware.common.Constants.ON;
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
import static com.sentrysoftware.metricshub.hardware.common.Constants.VM_1_ONLINE;
import static com.sentrysoftware.metricshub.hardware.common.Constants.VM_OFFLINE_2;
import static com.sentrysoftware.metricshub.hardware.common.Constants.VM_ONLINE_3;
import static com.sentrysoftware.metricshub.hardware.common.Constants.VM_ONLINE_BAD_POWER_SHARE_5;
import static com.sentrysoftware.metricshub.hardware.common.Constants.VM_ONLINE_NO_POWER_SHARE_4;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ENERGY_VM_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ESTIMATED_ENERGY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ESTIMATED_POWER;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_POWER_VM_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_VM_POWER_SHARE_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_VM_POWER_STATE_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.POWER_SOURCE_ID_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(new Date().getTime())
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();

		// Initialize the host and other monitors
		final Monitor host = Monitor
			.builder()
			.id(KnownMonitorType.HOST.getKey())
			.type(KnownMonitorType.HOST.getKey())
			.build();
		host.setAsEndpoint();

		// Set a previous collected host power value
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric previousPowerValue = metricFactory.collectNumberMetric(
			host,
			HW_HOST_ESTIMATED_POWER,
			60.0,
			telemetryManager.getStrategyTime() - 120 * 1000
		);
		previousPowerValue.save();

		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();
		metricFactory.collectNumberMetric(cpu, HW_CPU_POWER, 60.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(cpu, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		metricFactory.collectNumberMetric(memory, HW_MEMORY_POWER, 4.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(memory, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		metricFactory.collectNumberMetric(disk, HW_PHYSICAL_DISK_POWER, 6.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(disk, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			diskNoPower,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			missingDisk,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		// Add the previously created monitors to telemetry manager
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
		assertNotNull(host.getMetric(HW_HOST_ESTIMATED_POWER, NumberMetric.class));
		assertEquals(54.31, host.getMetric(HW_HOST_ESTIMATED_POWER, NumberMetric.class).getValue());
		assertNotNull(host.getMetric(HW_HOST_ESTIMATED_ENERGY, NumberMetric.class));
		assertEquals(6517.200000000001, host.getMetric(HW_HOST_ESTIMATED_ENERGY, NumberMetric.class).getValue());
	}

	private static Monitor buildMonitor(final String monitorType, final String id) {
		return Monitor.builder().id(id).type(monitorType).build();
	}

	@Test
	void testRunWithVmMonitor() {
		// Create the metric factory to collect metrics

		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());

		// Prepare the monitors and their metrics

		final Monitor vmOnline1 = buildMonitor(KnownMonitorType.VM.getKey(), VM_1_ONLINE);
		vmOnline1.addMetric(HW_VM_POWER_STATE_METRIC, StateSetMetric.builder().value(ON).build());
		metricFactory.collectNumberMetric(vmOnline1, HW_VM_POWER_SHARE_METRIC, 5.0, telemetryManager.getStrategyTime());

		final Monitor vmOffline2 = buildMonitor(KnownMonitorType.VM.getKey(), VM_OFFLINE_2);
		vmOffline2.addMetric(HW_VM_POWER_STATE_METRIC, StateSetMetric.builder().value("Off").build());
		metricFactory.collectNumberMetric(vmOffline2, HW_VM_POWER_SHARE_METRIC, 10.0, telemetryManager.getStrategyTime());

		final Monitor vmOnline3 = buildMonitor(KnownMonitorType.VM.getKey(), VM_ONLINE_3);
		vmOnline3.addMetric(HW_VM_POWER_STATE_METRIC, StateSetMetric.builder().value(ON).build());
		metricFactory.collectNumberMetric(vmOnline3, HW_VM_POWER_SHARE_METRIC, 5.0, telemetryManager.getStrategyTime());

		final Monitor vmOnlineNoPowerShare4 = buildMonitor(KnownMonitorType.VM.getKey(), VM_ONLINE_NO_POWER_SHARE_4);
		vmOnlineNoPowerShare4.addMetric(HW_VM_POWER_STATE_METRIC, StateSetMetric.builder().value(ON).build());

		final Monitor vmOnlineBadPowerShare5 = buildMonitor(KnownMonitorType.VM.getKey(), VM_ONLINE_BAD_POWER_SHARE_5);
		vmOnlineBadPowerShare5.addMetric(HW_VM_POWER_STATE_METRIC, StateSetMetric.builder().value(ON).build());
		metricFactory.collectNumberMetric(
			vmOnlineBadPowerShare5,
			HW_VM_POWER_SHARE_METRIC,
			-15.0,
			telemetryManager.getStrategyTime()
		);

		// Create the host monitor
		final Monitor host = buildMonitor(KnownMonitorType.HOST.getKey(), HOST_1);
		host.setAsEndpoint();

		// Set the host monitor estimated power
		metricFactory.collectNumberMetric(host, HW_HOST_ESTIMATED_POWER, 100.0, telemetryManager.getStrategyTime());

		// Add the created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), HOST_1);
		telemetryManager.addNewMonitor(vmOnline1, KnownMonitorType.VM.getKey(), VM_1_ONLINE);
		telemetryManager.addNewMonitor(vmOffline2, KnownMonitorType.VM.getKey(), VM_OFFLINE_2);
		telemetryManager.addNewMonitor(vmOnline3, KnownMonitorType.VM.getKey(), VM_ONLINE_3);
		telemetryManager.addNewMonitor(vmOnlineNoPowerShare4, KnownMonitorType.VM.getKey(), VM_ONLINE_NO_POWER_SHARE_4);
		telemetryManager.addNewMonitor(vmOnlineBadPowerShare5, KnownMonitorType.VM.getKey(), VM_ONLINE_BAD_POWER_SHARE_5);

		// Add power source id attribute to the created VM monitors
		vmOnline1.addAttribute(POWER_SOURCE_ID_ATTRIBUTE, host.getId());

		vmOffline2.addAttribute(POWER_SOURCE_ID_ATTRIBUTE, host.getId());

		vmOnline3.addAttribute(POWER_SOURCE_ID_ATTRIBUTE, host.getId());

		vmOnlineNoPowerShare4.addAttribute(POWER_SOURCE_ID_ATTRIBUTE, host.getId());

		vmOnlineBadPowerShare5.addAttribute(POWER_SOURCE_ID_ATTRIBUTE, host.getId());

		// Set previous power values
		NumberMetric previousPowerValue = metricFactory.collectNumberMetric(
			vmOnline1,
			HW_POWER_VM_METRIC,
			10.0,
			telemetryManager.getStrategyTime() - 120 * 1000
		);
		previousPowerValue.save();

		previousPowerValue =
			metricFactory.collectNumberMetric(
				vmOffline2,
				HW_POWER_VM_METRIC,
				1.0,
				telemetryManager.getStrategyTime() - 120 * 1000
			);
		previousPowerValue.save();

		previousPowerValue =
			metricFactory.collectNumberMetric(
				vmOnline3,
				HW_POWER_VM_METRIC,
				2.0,
				telemetryManager.getStrategyTime() - 120 * 1000
			);
		previousPowerValue.save();

		previousPowerValue =
			metricFactory.collectNumberMetric(
				vmOnlineNoPowerShare4,
				HW_POWER_VM_METRIC,
				12.0,
				telemetryManager.getStrategyTime() - 120 * 1000
			);
		previousPowerValue.save();

		previousPowerValue =
			metricFactory.collectNumberMetric(
				vmOnlineBadPowerShare5,
				HW_POWER_VM_METRIC,
				5.0,
				telemetryManager.getStrategyTime() - 120 * 1000
			);
		previousPowerValue.save();

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		assertEquals(50.0, CollectHelper.getNumberMetricValue(vmOnline1, HW_POWER_VM_METRIC, false));
		assertEquals(6000, CollectHelper.getNumberMetricValue(vmOnline1, HW_ENERGY_VM_METRIC, false));

		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOffline2, HW_POWER_VM_METRIC, false));
		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOffline2, HW_ENERGY_VM_METRIC, false));

		assertEquals(50.0, CollectHelper.getNumberMetricValue(vmOnline3, HW_POWER_VM_METRIC, false));
		assertEquals(6000, CollectHelper.getNumberMetricValue(vmOnline3, HW_ENERGY_VM_METRIC, false));

		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOnlineNoPowerShare4, HW_POWER_VM_METRIC, false));
		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOnlineNoPowerShare4, HW_ENERGY_VM_METRIC, false));

		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOnlineBadPowerShare5, HW_POWER_VM_METRIC, false));
		assertEquals(0.0, CollectHelper.getNumberMetricValue(vmOnlineBadPowerShare5, HW_ENERGY_VM_METRIC, false));
	}
}
