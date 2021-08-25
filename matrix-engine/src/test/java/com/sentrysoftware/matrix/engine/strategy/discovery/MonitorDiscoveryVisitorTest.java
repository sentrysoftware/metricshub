package com.sentrysoftware.matrix.engine.strategy.discovery;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
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
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ATTACHED_TO_DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MonitorDiscoveryVisitorTest {

	private static final String _0 = "0";
	private static final String MY_CONNECTOR_NAME = "myConnector.connector";
	private static final String MONITOR_NAME = "Monitor x";
	private static final String ENCLOSURE_NAME = "Computer: PowerEdge 54dsf (Dell 2200)";
	private static final String MODEL_VALUE = "2200";
	private static final String POWER_EDGE_54DSF = "PowerEdge 54dsf";
	private static final String ID = "1.1";
	private static final String DELL = "Dell";
	private static final String MODEL = "model";
	private static final String VENDOR = "vendor";
	private static final String DISPLAY_ID = "displayId";
	private static final String DEVICE_ID = "deviceId";
	private static final String DEVICE_ID1 = "deviceId1";
	private static final String DEVICE_ID2 = "deviceId2";
	private static final String ECS1_01 = "ecs1-01";
	private static final String VOLTAGE_ID = "myConnector.connector_voltage_ecs1-01_1.1";
	private static final String TEMPERATURE_ID = "myConnector.connector_temperature_ecs1-01_1.1";
	private static final String TAPE_DRIVE_ID = "myConnector.connector_tapedrive_ecs1-01_1.1";
	private static final String POWER_SUPPLY_ID = "myConnector.connector_powersupply_ecs1-01_1.1";
	private static final String PHYSICAL_DISK_ID = "myConnector.connector_physicaldisk_ecs1-01_1.1";
	private static final String OTHER_DEVICE_ID = "myConnector.connector_otherdevice_ecs1-01_1.1";
	private static final String NETWORK_CARD_ID = "myConnector.connector_networkcard_ecs1-01_1.1";
	private static final String MEMORY_ID = "myConnector.connector_memory_ecs1-01_1.1";
	private static final String BATTERY_ID = "myConnector.connector_battery_ecs1-01_1.1";
	private static final String BLADE_ID = "myConnector.connector_blade_ecs1-01_1.1";
	private static final String CPU_ID = "myConnector.connector_cpu_ecs1-01_1.1";
	private static final String CPU_CORE_ID = "myConnector.connector_cpucore_ecs1-01_1.1";
	private static final String DISK_CONTROLLER_ID = "myConnector.connector_diskcontroller_ecs1-01_1.1";
	private static final String DISK_CONTROLLER_MONITOR_X = "Disk Controller: Monitor x";
	private static final String LED_ID = "myConnector.connector_led_ecs1-01_1.1";
	private static final String LOGICAL_DISK_ID = "myConnector.connector_logicaldisk_ecs1-01_1.1";
	private static final String LUN_ID = "myConnector.connector_lun_ecs1-01_1.1";
	private static final String ENCLOSURE_ID = "myConnector.connector_enclosure_ecs1-01_1.1";
	private static final String FAN_ID = "myConnector.connector_fan_ecs1-01_1.1";
	private static final String ROBOTIC_ID = "myConnector.connector_robotic_ecs1-01_1.1";


	@Test
	void testVisitBattery() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final MonitorBuildingInfo buildingInfo = createBuildingInfo(hostMonitoring, MonitorType.BATTERY);
		new MonitorDiscoveryVisitor(buildingInfo).visit((Battery) MonitorType.BATTERY.getMetaMonitor());
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		metadata.put(DEVICE_ID, ID);
		metadata.put(HardwareConstants.ID_COUNT, _0);
		metadata.put(DISPLAY_ID, MONITOR_NAME);
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(BATTERY_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.BATTERY)
				.extendedType(MonitorType.BATTERY.getName())
				.alertRules(MonitorType.BATTERY.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(BLADE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.BLADE)
				.extendedType(MonitorType.BLADE.getName())
				.alertRules(MonitorType.BLADE.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(CPU_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.CPU)
				.extendedType(MonitorType.CPU.getName())
				.alertRules(MonitorType.CPU.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(CPU_CORE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.CPU_CORE)
				.extendedType(MonitorType.CPU_CORE.getName())
				.alertRules(MonitorType.CPU_CORE.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(DISK_CONTROLLER_ID)
				.name(DISK_CONTROLLER_MONITOR_X)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.DISK_CONTROLLER)
				.extendedType(MonitorType.DISK_CONTROLLER.getName())
				.alertRules(MonitorType.DISK_CONTROLLER.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

		final Map<String, Monitor> monitors = hostMonitoring.selectFromType(MonitorType.DISK_CONTROLLER);

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
				.alertRules(MonitorType.ENCLOSURE.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(FAN_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getName())
				.alertRules(MonitorType.FAN.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LED_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LED)
				.extendedType(MonitorType.LED.getName())
				.alertRules(MonitorType.LED.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LOGICAL_DISK_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LOGICAL_DISK)
				.extendedType(MonitorType.LOGICAL_DISK.getName())
				.alertRules(MonitorType.LOGICAL_DISK.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(LUN_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.LUN)
				.extendedType(MonitorType.LUN.getName())
				.alertRules(MonitorType.LUN.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(MEMORY_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.MEMORY)
				.extendedType(MonitorType.MEMORY.getName())
				.alertRules(MonitorType.MEMORY.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(NETWORK_CARD_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.NETWORK_CARD)
				.extendedType(MonitorType.NETWORK_CARD.getName())
				.alertRules(MonitorType.NETWORK_CARD.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(OTHER_DEVICE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.OTHER_DEVICE)
				.extendedType(MonitorType.OTHER_DEVICE.getName())
				.alertRules(MonitorType.OTHER_DEVICE.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(PHYSICAL_DISK_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.extendedType(MonitorType.PHYSICAL_DISK.getName())
				.alertRules(MonitorType.PHYSICAL_DISK.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(POWER_SUPPLY_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.POWER_SUPPLY)
				.extendedType(MonitorType.POWER_SUPPLY.getName())
				.alertRules(MonitorType.POWER_SUPPLY.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(TAPE_DRIVE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.TAPE_DRIVE)
				.extendedType(MonitorType.TAPE_DRIVE.getName())
				.alertRules(MonitorType.TAPE_DRIVE.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(TEMPERATURE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.TEMPERATURE)
				.extendedType(MonitorType.TEMPERATURE.getName())
				.alertRules(MonitorType.TEMPERATURE.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedMonitor = Monitor.builder()
				.id(VOLTAGE_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.VOLTAGE)
				.extendedType(MonitorType.VOLTAGE.getName())
				.alertRules(MonitorType.VOLTAGE.getMetaMonitor().getStaticAlertRules())
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
		metadata.put(TARGET_FQDN, null);


		final Monitor expectedMonitor = Monitor.builder()
				.id(ROBOTIC_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.ROBOTIC)
				.extendedType(MonitorType.ROBOTIC.getName())
				.alertRules(MonitorType.ROBOTIC.getMetaMonitor().getStaticAlertRules())
				.build();
		expectedMonitor.setAsPresent();

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
		metadata.put(TARGET_FQDN, null);

		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(MONITOR_NAME)
				.parentId(ECS1_01)
				.targetId(ECS1_01)
				.metadata(metadata)
				.monitorType(MonitorType.FAN)
				.extendedType(MonitorType.FAN.getName())
				.build();
		expectedFan.setAsPresent();

		final Map<String, Monitor> fans = hostMonitoring.selectFromType(MonitorType.FAN);

		assertEquals(expectedFan, fans.values().stream().findFirst().get());

	}
	@Test
	void testResolveParent() {

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

		MonitorDiscoveryVisitor monitorDiscoveryVisitor = new MonitorDiscoveryVisitor(buildingInfo);

		// rawAttachedToDeviceId != null, monitor != null
		metadata.put(ATTACHED_TO_DEVICE_ID, DEVICE_ID);
		Monitor expected = Monitor
			.builder()
			.id(DEVICE_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.parentId(targetMonitor.getId())
			.targetId(ECS1_01)
			.name(ECS1_01)
			.extendedType(COMPUTER)
			.build();
		hostMonitoring.addMonitor(expected);
		assertEquals(expected, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId != null, monitor == null
		hostMonitoring.removeMonitor(expected);
		assertEquals(targetMonitor, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null
		metadata.clear();
		assertEquals(targetMonitor, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null, enclosures == null
		hostMonitoring.setMonitors(Map.of(MonitorType.TARGET, Map.of(targetMonitor.getId(), targetMonitor)));
		assertEquals(targetMonitor, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null, enclosures is empty
		hostMonitoring.setMonitors(Map.of(MonitorType.ENCLOSURE, Collections.emptyMap()));
		assertEquals(targetMonitor, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null, enclosures != null, enclosures is not empty
		// one COMPUTER-type enclosure found
		hostMonitoring.setMonitors(Map.of(MonitorType.ENCLOSURE, Map.of(DEVICE_ID, expected)));
		assertEquals(expected, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null, enclosures != null, enclosures is not empty
		// no COMPUTER-type enclosure found, enclosures.size() == 1
		expected.setExtendedType(null);
		assertEquals(expected, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));

		// rawAttachedToDeviceId == null, enclosures != null, enclosures is not empty
		// no COMPUTER-type enclosure found, enclosures.size() != 1
		Monitor enclosure1 = Monitor
			.builder()
			.id(DEVICE_ID1)
			.monitorType(MonitorType.ENCLOSURE)
			.parentId(targetMonitor.getId())
			.targetId(ECS1_01)
			.name(ECS1_01)
			.build();
		Monitor enclosure2 = Monitor
			.builder()
			.id(DEVICE_ID2)
			.monitorType(MonitorType.ENCLOSURE)
			.parentId(targetMonitor.getId())
			.targetId(ECS1_01)
			.name(ECS1_01)
			.build();
		hostMonitoring.setMonitors(Map.of(MonitorType.ENCLOSURE, Map.of(DEVICE_ID1, enclosure1, DEVICE_ID2, enclosure2)));
		assertEquals(targetMonitor, monitorDiscoveryVisitor.resolveParent(metadata, hostMonitoring, targetMonitor));
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
}
