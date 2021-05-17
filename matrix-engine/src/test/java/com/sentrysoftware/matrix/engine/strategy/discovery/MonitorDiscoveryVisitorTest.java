package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.DiskEnclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotic;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

class MonitorDiscoveryVisitorTest {

	private static final String _0 = "0";
	private static final String ROBOTIC_MONITOR_X = "Robotic: Monitor x";
	private static final String FAN_ID = "myConnecctor.connector_fan_ecs1-01_1.1";
	private static final String ROBOTIC_ID = "myConnecctor.connector_robotic_ecs1-01_1.1";
	private static final String ENCLOSURE_NAME_MODEL_WITH_VENDOR = "Computer: PowerEdge 54dsf (2200 Dell)";
	private static final String STORAGE_NAME = "Storage: PowerEdge 54dsf";
	private static final String OTHER = "Other:";
	private static final String STORAGE_0 = "Storage:  (0)";
	private static final String ENCLOSURE_NAME_NO_MODEL_NO_VENDOR = "Computer: PowerEdge 54dsf (Linux computer)";
	private static final String ENCLOSURE_NAME_NO_MODEL = "Computer: PowerEdge 54dsf (Dell)";
	private static final String ENCLOSURE_NAME_NO_VENDOR = "Computer: PowerEdge 54dsf (2200)";
	private static final String UNKNOWN_COMPUTER = "Unknown computer";
	private static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS computer";
	private static final String HP_TRU64_COMPUTER = "HP Tru64 computer";
	private static final String LINUX_COMPUTER = "Linux computer";
	private static final String WINDOWS_COMPUTER = "Windows computer";
	private static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";
	private static final String FAN_0 = "Fan: 0";
	private static final String FAN_1_1 = "Fan: 1.1";
	private static final String MY_CONNECTOR_NAME = "myConnecctor.connector";
	private static final String FAN_NAME = "Fan: Monitor x";
	private static final String MONITOR_NAME = "Monitor x";
	private static final String ENCLOSURE_NAME = "Computer: PowerEdge 54dsf (Dell 2200)";
	private static final String MODEL_VALUE = "2200";
	private static final String POWER_EDGE_54DSF = "PowerEdge 54dsf";
	private static final String ID = "1.1";
	private static final String DELL = "Dell";
	private static final String MODEL_WITH_VENDOR = MODEL_VALUE + " " + DELL;
	private static final String MODEL = "model";
	private static final String VENDOR = "vendor";
	private static final String DISPLAY_ID = "displayId";
	private static final String DEVICE_ID = "deviceId";
	private static final String ECS1_01 = "ecs1-01";
	private static final String VOLTAGE_ID = "myConnecctor.connector_voltage_ecs1-01_1.1";
	private static final String VOLTAGE_MONITOR_X = "Voltage: Monitor x";
	private static final String TEMPERATURE_ID = "myConnecctor.connector_temperature_ecs1-01_1.1";
	private static final String TEMPERATURE_MONITOR_X = "Temperature: Monitor x";
	private static final String TAPE_DRIVE_ID = "myConnecctor.connector_tapedrive_ecs1-01_1.1";
	private static final String TAPE_DRIVE_MONITOR_X = "TapeDrive: Monitor x";
	private static final String POWER_SUPPLY_ID = "myConnecctor.connector_powersupply_ecs1-01_1.1";
	private static final String POWER_SUPPLY_MONITOR_X = "PowerSupply: Monitor x";
	private static final String PHYSICAL_DISK_ID = "myConnecctor.connector_physicaldisk_ecs1-01_1.1";
	private static final String PHYSICAL_DISK_MONITOR_X = "PhysicalDisk: Monitor x";
	private static final String OTHER_DEVICE_ID = "myConnecctor.connector_otherdevice_ecs1-01_1.1";
	private static final String OTHER_DEVICE_MONITOR_X = "OtherDevice: Monitor x";
	private static final String NETWORK_CARD_ID = "myConnecctor.connector_networkcard_ecs1-01_1.1";
	private static final String NETWORK_CARD_MONITOR_X = "NetworkCard: Monitor x";
	private static final String MEMORY_ID = "myConnecctor.connector_memory_ecs1-01_1.1";
	private static final String MEMORY_MONITOR_X = "Memory: Monitor x";
	private static final String BATTERY_ID = "myConnecctor.connector_battery_ecs1-01_1.1";
	private static final String BATTERY_MONITOR_X = "Battery: Monitor x";
	private static final String BLADE_ID = "myConnecctor.connector_blade_ecs1-01_1.1";
	private static final String BLADE_MONITOR_X = "Blade: Monitor x";
	private static final String CPU_ID = "myConnecctor.connector_cpu_ecs1-01_1.1";
	private static final String CPU_MONITOR_X = "CPU: Monitor x";
	private static final String CPU_CORE_ID = "myConnecctor.connector_cpucore_ecs1-01_1.1";
	private static final String CPU_CORE_MONITOR_X = "CpuCore: Monitor x";
	private static final String DISK_CONTROLLER_ID = "myConnecctor.connector_diskcontroller_ecs1-01_1.1";
	private static final String DISK_CONTROLLER_MONITOR_X = "DiskController: Monitor x";
	private static final String DISK_ENCLOSURE_ID = "myConnecctor.connector_diskenclosure_ecs1-01_1.1";
	private static final String DISK_ENCLOSURE_MONITOR_X = "DiskEnclosure: Monitor x";
	private static final String FAN_MONITOR_X = "Fan: Monitor x";
	private static final String LED_ID = "myConnecctor.connector_led_ecs1-01_1.1";
	private static final String LED_MONITOR_X = "LED: Monitor x";
	private static final String LOGICAL_DISK_ID = "myConnecctor.connector_logicaldisk_ecs1-01_1.1";
	private static final String LOGICAL_DISK_MONITOR_X = "LogicalDisk: Monitor x";
	private static final String LUN_ID = "myConnecctor.connector_lun_ecs1-01_1.1";
	private static final String LUN_MONITOR_X = "Lun: Monitor x";
	private static final String ENCLOSURE_ID = "myConnecctor.connector_enclosure_ecs1-01_1.1";

	@Test
	void testVisitBattery() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.BATTERY);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Battery) MonitorType.BATTERY.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(BATTERY_ID)
				.name(BATTERY_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.BATTERY)
				.extendedType(MonitorType.BATTERY.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.BATTERY);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitBlade() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.BLADE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Blade) MonitorType.BLADE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(BLADE_ID)
				.name(BLADE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.BLADE)
				.extendedType(MonitorType.BLADE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.BLADE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitCpu() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.CPU);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Cpu) MonitorType.CPU.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(CPU_ID)
				.name(CPU_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.CPU)
				.extendedType(MonitorType.CPU.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.CPU);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitCpuCore() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.CPU_CORE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((CpuCore) MonitorType.CPU_CORE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(CPU_CORE_ID)
				.name(CPU_CORE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.CPU_CORE)
				.extendedType(MonitorType.CPU_CORE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.CPU_CORE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitDiskController() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.DISK_CONTROLLER);
		new MonitorDiscoveryVisitor(buildingInfo).visit((DiskController) MonitorType.DISK_CONTROLLER.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(DISK_CONTROLLER_ID)
				.name(DISK_CONTROLLER_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.DISK_CONTROLLER)
				.extendedType(MonitorType.DISK_CONTROLLER.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.DISK_CONTROLLER);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitDiskEnclosure() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.DISK_ENCLOSURE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((DiskEnclosure) MonitorType.DISK_ENCLOSURE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(DISK_ENCLOSURE_ID)
				.name(DISK_ENCLOSURE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.DISK_ENCLOSURE)
				.extendedType(MonitorType.DISK_ENCLOSURE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.DISK_ENCLOSURE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitEnclosure() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.metadata(metadata)
				.build();

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(hostMonitoring)
				.targetType(TargetType.LINUX)
				.targetMonitor(targetMonitor)
				.hostname(ECS1_01)
				.build();

		new MonitorDiscoveryVisitor(buildingInfo).visit((Enclosure) MonitorType.ENCLOSURE.getMetaMonitor());

		final Monitor expectedMonitor = Monitor.builder()
				.id(ENCLOSURE_ID)
				.name(ENCLOSURE_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(HardwareConstants.COMPUTER)
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.ENCLOSURE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitFan() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.FAN);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Fan) MonitorType.FAN.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.FAN);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitLed() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.LED);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Led) MonitorType.LED.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LED_ID)
				.name(LED_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LED)
				.extendedType(MonitorType.LED.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.LED);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitLogicalDisk() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.LOGICAL_DISK);
		new MonitorDiscoveryVisitor(buildingInfo).visit((LogicalDisk) MonitorType.LOGICAL_DISK.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LOGICAL_DISK_ID)
				.name(LOGICAL_DISK_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LOGICAL_DISK)
				.extendedType(MonitorType.LOGICAL_DISK.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.LOGICAL_DISK);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitLun() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.LUN);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Lun) MonitorType.LUN.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LUN_ID)
				.name(LUN_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LUN)
				.extendedType(MonitorType.LUN.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.LUN);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitMemory() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.MEMORY);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Memory) MonitorType.MEMORY.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(MEMORY_ID)
				.name(MEMORY_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.MEMORY)
				.extendedType(MonitorType.MEMORY.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.MEMORY);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitNetworkCard() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.NETWORK_CARD);
		new MonitorDiscoveryVisitor(buildingInfo).visit((NetworkCard) MonitorType.NETWORK_CARD.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(NETWORK_CARD_ID)
				.name(NETWORK_CARD_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.NETWORK_CARD)
				.extendedType(MonitorType.NETWORK_CARD.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.NETWORK_CARD);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitOtherDevice() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.OTHER_DEVICE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((OtherDevice) MonitorType.OTHER_DEVICE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(OTHER_DEVICE_ID)
				.name(OTHER_DEVICE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.OTHER_DEVICE)
				.extendedType(MonitorType.OTHER_DEVICE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.OTHER_DEVICE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitPhysicalDisk() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.PHYSICAL_DISK);
		new MonitorDiscoveryVisitor(buildingInfo).visit((PhysicalDisk) MonitorType.PHYSICAL_DISK.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(PHYSICAL_DISK_ID)
				.name(PHYSICAL_DISK_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.extendedType(MonitorType.PHYSICAL_DISK.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.PHYSICAL_DISK);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitPowerSupply() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.POWER_SUPPLY);
		new MonitorDiscoveryVisitor(buildingInfo).visit((PowerSupply) MonitorType.POWER_SUPPLY.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(POWER_SUPPLY_ID)
				.name(POWER_SUPPLY_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.POWER_SUPPLY)
				.extendedType(MonitorType.POWER_SUPPLY.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.POWER_SUPPLY);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitTapeDrive() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.TAPE_DRIVE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((TapeDrive) MonitorType.TAPE_DRIVE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(TAPE_DRIVE_ID)
				.name(TAPE_DRIVE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.TAPE_DRIVE)
				.extendedType(MonitorType.TAPE_DRIVE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.TAPE_DRIVE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitTemperature() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.TEMPERATURE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Temperature) MonitorType.TEMPERATURE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(TEMPERATURE_ID)
				.name(TEMPERATURE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.TEMPERATURE)
				.extendedType(MonitorType.TEMPERATURE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.TEMPERATURE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitVoltage() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.VOLTAGE);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Voltage) MonitorType.VOLTAGE.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedMonitor = Monitor.builder()
				.id(VOLTAGE_ID)
				.name(VOLTAGE_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.VOLTAGE)
				.extendedType(MonitorType.VOLTAGE.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.VOLTAGE);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testVisitRobotic() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.ROBOTIC);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Robotic) MonitorType.ROBOTIC.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);


		final Monitor expectedMonitor = Monitor.builder()
				.id(ROBOTIC_ID)
				.name(ROBOTIC_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ROBOTIC)
				.extendedType(MonitorType.ROBOTIC.getName())
				.build();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.ROBOTIC);

		assertEquals(expectedMonitor, monitors.values().stream().findFirst().get());

	}

	@Test
	void testCreateMonitorNoName() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		final Monitor monitor = Monitor
				.builder()
				.monitorType(MonitorType.FAN)
				.metadata(metadata)
				.build();

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.FAN)
				.monitor(monitor)
				.hostMonitoring(hostMonitoring)
				.targetType(TargetType.LINUX)
				.targetMonitor(targetMonitor)
				.hostname(ECS1_01)
				.build();

		new MonitorDiscoveryVisitor(buildingInfo).createMonitor(null, null);

		assertNull(hostMonitoring.selectFromType(MonitorType.FAN));

	}

	@Test
	void testCreateMonitorNoDeviceId() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		final Monitor monitor = Monitor
				.builder()
				.monitorType(MonitorType.FAN)
				.metadata(metadata)
				.build();

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.FAN)
				.monitor(monitor)
				.hostMonitoring(hostMonitoring)
				.targetType(TargetType.LINUX)
				.targetMonitor(targetMonitor)
				.hostname(ECS1_01)
				.build();

		new MonitorDiscoveryVisitor(buildingInfo).createMonitor(MONITOR_NAME, null);

		assertNull(hostMonitoring.selectFromType(MonitorType.FAN));

	}

	@Test
	void testCreateMonitor() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.FAN);

		new MonitorDiscoveryVisitor(buildingInfo).createMonitor(MONITOR_NAME, null);

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getName())
				.build();

		final Map<String, Monitor> fans = hostMonitoring.selectFromType(MonitorType.FAN);

		assertEquals(expectedFan, fans.values().stream().findFirst().get());

	}

	private MonitorBuildingInfo createBuildingInfo(final IHostMonitoring hostMonitoring, final MonitorType monitorType) {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);

		final Monitor monitor = Monitor
				.builder()
				.monitorType(monitorType)
				.metadata(metadata)
				.build();

		final Monitor targetMonitor = Monitor
				.builder()
				.id(ECS1_01)
				.parentId(null)
				.targetId(ECS1_01)
				.name(ECS1_01)
				.monitorType(MonitorType.TARGET)
				.build();

		return MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(monitorType)
				.monitor(monitor)
				.hostMonitoring(hostMonitoring)
				.targetType(TargetType.LINUX)
				.targetMonitor(targetMonitor)
				.hostname(ECS1_01)
				.build();

	}

	@Test
	void testBuildEnclosureStorageWithDisplayId() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.STORAGE);
		
		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(STORAGE_NAME, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureOther() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, "Other");

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(OTHER, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureNameNoModelNoVendorStorage() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.STORAGE);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(STORAGE_0, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureNameNoModelNoVendor() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(ENCLOSURE_NAME_NO_MODEL_NO_VENDOR, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureNameNoModel() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(ENCLOSURE_NAME_NO_MODEL, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureNameNoVendor() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(ENCLOSURE_NAME_NO_VENDOR, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureNameModelContainsVendor() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_WITH_VENDOR);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(ENCLOSURE_NAME_MODEL_WITH_VENDOR, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildEnclosureName() {

		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(DISPLAY_ID, POWER_EDGE_54DSF);
		metadata.put(VENDOR, DELL);
		metadata.put(MODEL, MODEL_VALUE);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

		final Monitor monitor = Monitor
				.builder()
				.metadata(metadata)
				.build();

		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
				.builder()
				.connectorName(MY_CONNECTOR_NAME)
				.monitorType(MonitorType.ENCLOSURE)
				.monitor(monitor)
				.hostMonitoring(new HostMonitoring())
				.targetType(TargetType.LINUX)
				.targetMonitor(new Monitor())
				.hostname(ECS1_01)
				.build();

		assertEquals(ENCLOSURE_NAME, new MonitorDiscoveryVisitor(buildingInfo).buildEnclosureName());

	}

	@Test
	void testBuildGenericName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(DEVICE_ID, ID);
			metadata.put(DISPLAY_ID, MONITOR_NAME);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName(MY_CONNECTOR_NAME)
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname(ECS1_01)
					.build();
			assertEquals(FAN_NAME, new MonitorDiscoveryVisitor(buildingInfo).buildGenericName());
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(DEVICE_ID, ID);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName(MY_CONNECTOR_NAME)
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname(ECS1_01)
					.build();

			assertEquals(FAN_1_1, new MonitorDiscoveryVisitor(buildingInfo).buildGenericName());
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName(MY_CONNECTOR_NAME)
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname(ECS1_01)
					.build();
			assertEquals(FAN_0, new MonitorDiscoveryVisitor(buildingInfo).buildGenericName());
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, " ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName(MY_CONNECTOR_NAME)
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname(ECS1_01)
					.build();

			assertNull(new MonitorDiscoveryVisitor(buildingInfo).buildGenericName());
		}

	}

	@Test
	void testHandleComputerDisplayName() {

		{
			final Monitor target = Monitor
					.builder()
					.metadata(Map.of(HardwareConstants.LOCATION, HardwareConstants.REMOTE))
					.build();

			assertEquals(WINDOWS_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.MS_WINDOWS));
			assertEquals(LINUX_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.LINUX));
			assertEquals(HP_TRU64_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.HP_TRU64_UNIX));
			assertEquals(HP_OPEN_VMS_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.HP_OPEN_VMS));
			assertEquals(UNKNOWN_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target,  TargetType.STORAGE));
			assertEquals(UNKNOWN_COMPUTER, MonitorDiscoveryVisitor.handleComputerDisplayName(target,  TargetType.NETWORK_SWITCH));
		}

		{
			final Monitor target = Monitor
					.builder()
					.metadata(Map.of(HardwareConstants.LOCATION, HardwareConstants.LOCALHOST))
					.build();

			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.MS_WINDOWS));
			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.LINUX));
			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.HP_TRU64_UNIX));
			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target, TargetType.HP_OPEN_VMS));
			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target,  TargetType.STORAGE));
			assertEquals(LOCALHOST_ENCLOSURE, MonitorDiscoveryVisitor.handleComputerDisplayName(target,  TargetType.NETWORK_SWITCH));
		}

	}

	@Test
	void testIsLocalhost() {

		assertFalse(MonitorDiscoveryVisitor.isLocalhost(null));
		assertFalse(MonitorDiscoveryVisitor.isLocalhost(Collections.emptyMap()));
		assertFalse(MonitorDiscoveryVisitor.isLocalhost(Map.of(HardwareConstants.LOCATION, HardwareConstants.REMOTE)));
		assertTrue(MonitorDiscoveryVisitor.isLocalhost(Map.of(HardwareConstants.LOCATION,  HardwareConstants.LOCALHOST)));

	}

}
