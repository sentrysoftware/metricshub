package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_LABEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BLADE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FAN_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCALHOST;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCAL_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RAID_LEVEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ROBOTIC_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STORAGE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

import com.sentrysoftware.matrix.engine.host.HostType;

class MonitorNameBuilderTest {

	@Test
	void testHandleComputerDisplayName() {

		{
			final Monitor target = Monitor.builder()
					.metadata(Map.of(LOCATION, REMOTE)).build();

			assertEquals(MonitorNameBuilder.WINDOWS_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.MS_WINDOWS));
			assertEquals(MonitorNameBuilder.LINUX_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.LINUX));
			assertEquals(MonitorNameBuilder.HP_TRU64_UNIX_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.HP_TRU64_UNIX));
			assertEquals(MonitorNameBuilder.HP_OPEN_VMS_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.HP_OPEN_VMS));
			assertEquals(MonitorNameBuilder.STORAGE_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.STORAGE));
			assertEquals(MonitorNameBuilder.NETWORK_SWITCH_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.NETWORK_SWITCH));
		}

		{
			final Monitor target = Monitor.builder()
					.metadata(Map.of(LOCATION, LOCALHOST)).build();

			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.MS_WINDOWS));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.LINUX));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.HP_TRU64_UNIX));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.HP_OPEN_VMS));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.STORAGE));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, HostType.NETWORK_SWITCH));
		}

	}

	@Test
	void testIsLocalhost() {

		assertFalse(MonitorNameBuilder.isLocalhost(null));
		assertFalse(MonitorNameBuilder.isLocalhost(Collections.emptyMap()));
		assertFalse(MonitorNameBuilder.isLocalhost(Map.of(LOCATION, REMOTE)));
		assertTrue(MonitorNameBuilder.isLocalhost(Map.of(LOCATION, LOCALHOST)));

	}

	@Test
	void testBuildName() {

		assertEquals("type A: display 12345", MonitorNameBuilder
			.buildName(
				"type A",
				"display 12345",
				"dev 12345",
				"0",
				Pattern.compile("dev(ice)*", Pattern.CASE_INSENSITIVE),
				""
			));

		assertEquals("12345", MonitorNameBuilder
			.buildName(
				"",
				WHITE_SPACE,
				"Dev DEVICE 12345",
				"0",
				Pattern.compile("dev(ice)*", Pattern.CASE_INSENSITIVE),
				""
			));

		assertEquals("0 (info)", MonitorNameBuilder
			.buildName(
				"",
				WHITE_SPACE,
				"device 12345678901234567890",
				"0",
				Pattern.compile("dev(ice)*", Pattern.CASE_INSENSITIVE),
				"info"
			));

		assertEquals("type Z: 0 (info)", MonitorNameBuilder
			.buildName(
				"type Z",
				WHITE_SPACE,
				WHITE_SPACE,
				"0",
				Pattern.compile("dev(ice)*", Pattern.CASE_INSENSITIVE),
				"info"
			));
	}

	@Test
	void testBuildBatteryName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " battery,  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.BATTERY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildBatteryName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " battery,  1,1 ");
			metadata.put(DISPLAY_ID, "1.1");
			metadata.put(VENDOR, "Intel");
			metadata.put(MODEL, "CMOS");
			metadata.put(TYPE, "System Board CMOS Battery");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.BATTERY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1 (Intel CMOS - System Board CMOS Battery)",
					MonitorNameBuilder.buildBatteryName(buildingInfo));
		}
	}

	@Test
	void testBuildBladeName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " blade,  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.BLADE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildBladeName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " blade,  1,1 ");
			metadata.put(DISPLAY_ID, "1.1");
			metadata.put(BLADE_NAME, "Blade 123");
			metadata.put(MODEL, "model 1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.BLADE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1 (Blade 123 - model 1)", MonitorNameBuilder.buildBladeName(buildingInfo));
		}
	}

	@Test
	void testBuildCpuName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "CPU1.1");
			metadata.put(DISPLAY_ID, "CPU #1");
			metadata.put(VENDOR, "Intel");
			metadata.put(MODEL, "Xeon");
			metadata.put(MAXIMUM_SPEED, "3600.0");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.CPU)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("CPU #1 (Intel - Xeon - 3.60 GHz)", MonitorNameBuilder.buildCpuName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "CPU1,1");
			metadata.put(VENDOR, "Intel");
			metadata.put(MAXIMUM_SPEED, "999");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.CPU)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11 (Intel - 999 MHz)", MonitorNameBuilder.buildCpuName(buildingInfo));
		}
	}

	@Test
	void testBuildCpuCoreName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " proc  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.CPU_CORE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildCpuCoreName(buildingInfo));
		}
	}


	@Test
	void testBuildDiskControllerName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "1,1");
			metadata.put(DISPLAY_ID, "1.1");
			metadata.put(VENDOR, "vendor X");
			metadata.put(MODEL, "model 1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.DISK_CONTROLLER)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Disk Controller: 1.1 (vendor X model 1)",
					MonitorNameBuilder.buildDiskControllerName(buildingInfo));
		}
	}

	@Test
	void testBuildEnclosureName() {

		// Computer: with different model and vendor, no display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(VENDOR, "Dell");
			metadata.put(MODEL, "2200");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: Dell 2200", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: model with vendor & display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(VENDOR, "Dell");
			metadata.put(MODEL, "2200 Dell");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (2200 Dell)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no model
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(VENDOR, "Dell");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (Dell)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no vendor
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(MODEL, "2200");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (2200)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no model, no vendor
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE).monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (Linux Computer)",
					MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Storage: no model, no vendor, no display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: 1.1", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Storage: no model, no vendor, no display ID, and device ID too long
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "12345678901234567890");
			metadata.put(ID_COUNT, "1");
			metadata.put(TYPE, STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: 1", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Storage: no model, no vendor, but with display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(ID_COUNT, "0");
			metadata.put(DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(TYPE, STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: PowerEdge 54dsf", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Other
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(DEVICE_ID, "1.1");
			metadata.put(ID_COUNT, "0");
			metadata.put(TYPE, "Other");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Enclosure: 1.1", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}
	}


	@Test
	void testBuildFanName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "FAN 1.1");
			metadata.put(DISPLAY_ID, "Fan 1A 1.1 XYZ");
			metadata.put(FAN_TYPE, "1A");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.FAN).monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Fan 1A 1.1 XYZ (1A)", MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "FAN 1.1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1", MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(FAN_TYPE, "1A");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("0 (1A)", MonitorNameBuilder.buildFanName(buildingInfo));
		}
	}

	@Test
	void testBuildLedName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "LED 1.1");
			metadata.put(DISPLAY_ID, "LED #1");
			metadata.put(NAME, "Network");
			metadata.put(COLOR, "RED");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LED)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("LED #1 (Red - Network)", MonitorNameBuilder.buildLedName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "LED 1,1");
			metadata.put(COLOR, "green");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LED)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11 (Green)", MonitorNameBuilder.buildLedName(buildingInfo));
		}
	}

	@Test
	void testBuildLogicalDiskName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "disk01");
			metadata.put(DISPLAY_ID, "disk-01");
			metadata.put(SIZE, "1.09E13");
			metadata.put(RAID_LEVEL, "5");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LOGICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("disk-01 (RAID 5 - 9.9 TB)", MonitorNameBuilder.buildLogicalDiskName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "disk01");
			metadata.put(SIZE, "1073741824");
			metadata.put(RAID_LEVEL, "Raid 2");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LOGICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01 (Raid 2 - 1.0 GB)", MonitorNameBuilder.buildLogicalDiskName(buildingInfo));
		}
	}

	@Test
	void testBuildLunName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " LUN,1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LUN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildLunName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " LUN,  1 ");
			metadata.put(DISPLAY_ID, "LUN 1");
			metadata.put(LOCAL_DEVICE_NAME, "local 123");
			metadata.put(REMOTE_DEVICE_NAME, "remote 123");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.LUN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("LUN 1 (local 123 - remote 123)", MonitorNameBuilder.buildLunName(buildingInfo));
		}
	}

	@Test
	void testBuildMemoryName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " memory module 11 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.MEMORY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildMemoryName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "1.1");
			metadata.put(VENDOR, "Hynix Semiconductor (00AD00B300AD)");
			metadata.put(TYPE, "DDR4");
			metadata.put(SIZE, "16384.0");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.MEMORY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1 (Hynix Semiconductor (00AD00B300AD) - DDR4 - 16384 MB)", MonitorNameBuilder.buildMemoryName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "1.1");
			metadata.put(VENDOR, "Hynix Semiconductor (00AD00B300AD)");
			metadata.put(TYPE, "DDR4");
			metadata.put(SIZE, "49");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.MEMORY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1 (Hynix Semiconductor (00AD00B300AD) - DDR4)", MonitorNameBuilder.buildMemoryName(buildingInfo));
		}
	}

	@Test
	void testBuildNetworkCardName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "network 11");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.NETWORK_CARD)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildNetworkCardName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "0");
			metadata.put(VENDOR, "HP Ethernet Controller Interface 10/100 base-t");
			metadata.put(DEVICE_TYPE, "NIC");
			metadata.put(MODEL, "1234");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.NETWORK_CARD)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("0 (NIC - HP - 1234)", MonitorNameBuilder.buildNetworkCardName(buildingInfo));
		}
	}


	@Test
	void testBuildOtherDeviceName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "other device 11");
			metadata.put(ADDITIONAL_LABEL, "additional details");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.OTHER_DEVICE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("0 (additional details)", MonitorNameBuilder.buildOtherDeviceName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "01");
			metadata.put(DEVICE_TYPE, "type C");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.OTHER_DEVICE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("type C: 01", MonitorNameBuilder.buildOtherDeviceName(buildingInfo));
		}
	}


	@Test
	void testBuildPhysicalDiskName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "disk01");
			metadata.put(DISPLAY_ID, "disk-01");
			metadata.put(SIZE, "1E12");
			metadata.put(VENDOR, "HP");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.PHYSICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("disk-01 (HP - 1.0 TB)", MonitorNameBuilder.buildPhysicalDiskName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "disk01");
			metadata.put(SIZE, "1000000000");
			metadata.put(VENDOR, "HP");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.PHYSICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01 (HP - 1.0 GB)", MonitorNameBuilder.buildPhysicalDiskName(buildingInfo));
		}
	}

	@Test
	void testBuildPowerSupplyName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "power01");
			metadata.put(POWER_SUPPLY_POWER, "1000");
			metadata.put(POWER_SUPPLY_TYPE, "Dell");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.POWER_SUPPLY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01 (Dell - 1000 W)", MonitorNameBuilder.buildPowerSupplyName(buildingInfo));
		}
	}

	@Test
	void testBuildRoboticsName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " robotics,  1,1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ROBOTICS)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("111", MonitorNameBuilder.buildRoboticsName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, " robotics,  1,1,1 ");
			metadata.put(DISPLAY_ID, "1.1.1");
			metadata.put(VENDOR, "Quantum");
			metadata.put(MODEL, "Quantum 123");
			metadata.put(ROBOTIC_TYPE, "Tape Library");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.ROBOTICS)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1.1 (Quantum 123 - Tape Library)",
					MonitorNameBuilder.buildRoboticsName(buildingInfo));
		}
	}

	@Test
	void testBuildTapeDriveName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "tape drive, 01");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.TAPE_DRIVE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01", MonitorNameBuilder.buildTapeDriveName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "drive 12");
			metadata.put(DISPLAY_ID, "drive 12");
			metadata.put(VENDOR, "Quantum");
			metadata.put(MODEL, "Quantum 123");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.TAPE_DRIVE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("drive 12 (Quantum 123)",
					MonitorNameBuilder.buildTapeDriveName(buildingInfo));
		}
	}

	@Test
	void testBuildTemperatureName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "temperature sensor 101");
			metadata.put(TEMPERATURE_TYPE, "fan temperature");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.TEMPERATURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("101 (fan temperature)",
					MonitorNameBuilder.buildTemperatureName(buildingInfo));
		}
	}

	@Test
	void testBuildVoltageName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(ID_COUNT, "0");
			metadata.put(DEVICE_ID, "voltage 101");
			metadata.put(VOLTAGE_TYPE, "fan voltage");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector")
					.monitorType(MonitorType.VOLTAGE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.hostType(HostType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("101 (fan voltage)",
					MonitorNameBuilder.buildVoltageName(buildingInfo));
		}
	}

	@Test
	void testBuildVmName() {

		// metadata is null
		Monitor monitor = new Monitor();
		monitor.setMetadata(null);
		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo.builder().monitor(monitor).build();
		assertThrows(IllegalArgumentException.class, () -> MonitorNameBuilder.buildVmName(buildingInfo));

		// displayId is null
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, "vm 101");
		metadata.put(ID_COUNT, "0");

		monitor = Monitor
			.builder()
			.metadata(metadata)
			.build();

		final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
			.builder()
			.connectorName("myConnector")
			.monitorType(MonitorType.VM)
			.monitor(monitor)
			.hostMonitoring(new HostMonitoring())
			.hostType(HostType.LINUX)
			.targetMonitor(new Monitor())
			.hostname("ecs1-01")
			.build();

		assertEquals("101", MonitorNameBuilder.buildVmName(monitorBuildingInfo));

		// displayId is blank
		metadata.put(DISPLAY_ID, "         ");
		assertEquals("101", MonitorNameBuilder.buildVmName(monitorBuildingInfo));

		// displayId is neither null nor blank
		metadata.put(DISPLAY_ID, "displayId");
		assertEquals("displayId", MonitorNameBuilder.buildVmName(monitorBuildingInfo));
	}

	@Test
	void testBuildGpuName() {

		// metadata is null
		Monitor monitor = new Monitor();
		monitor.setMetadata(null);
		final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo.builder().monitor(monitor).build();
		assertThrows(IllegalArgumentException.class, () -> MonitorNameBuilder.buildGpuName(buildingInfo));

		// vendor is blank, model is blank
		final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		metadata.put(DEVICE_ID, "gpu NVIDIA 1");
		metadata.put(ID_COUNT, "0");
		metadata.put(VENDOR, "          ");
		metadata.put(MODEL, "          ");

		monitor = Monitor
			.builder()
			.metadata(metadata)
			.build();

		final MonitorBuildingInfo monitorBuildingInfo = MonitorBuildingInfo
			.builder()
			.connectorName("myConnector")
			.monitorType(MonitorType.GPU)
			.monitor(monitor)
			.hostMonitoring(new HostMonitoring())
			.hostType(HostType.LINUX)
			.targetMonitor(new Monitor())
			.hostname("ecs1-01")
			.build();

		assertEquals("NVIDIA 1", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// vendor or model (or both) are part of the built name
		metadata.put(VENDOR, "NVIDIA");
		metadata.put(MODEL, "N");
		assertEquals("NVIDIA 1", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// vendor is null, model is null, size is 0.0
		metadata.remove(VENDOR);
		metadata.remove(MODEL);
		assertEquals("NVIDIA 1", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// vendor is null, model is null, size > 0.0
		metadata.put(SIZE, "1024");
		assertEquals("NVIDIA 1 - 1.00 GB", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// size > 0.0, vendor is not null, model is not null, vendor is part of model
		metadata.put(VENDOR, "NVIDIA_VENDOR");
		metadata.put(MODEL, "NVIDIA_VENDOR_MODEL");
		assertEquals("NVIDIA 1 (NVIDIA_VENDOR_MODEL - 1.00 GB)", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// size > 0.0, vendor is not null, model is not null, vendor is not part of model
		metadata.put(VENDOR, "VENDOR");
		metadata.put(MODEL, "MODEL");
		assertEquals("NVIDIA 1 (VENDOR - MODEL - 1.00 GB)", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// size > 0.0, vendor is not null, model is null
		metadata.remove(MODEL);
		assertEquals("NVIDIA 1 (VENDOR - 1.00 GB)", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));

		// size > 0.0, vendor is null, model is not null
		metadata.remove(VENDOR);
		metadata.put(MODEL, "MODEL");
		assertEquals("NVIDIA 1 (MODEL - 1.00 GB)", MonitorNameBuilder.buildGpuName(monitorBuildingInfo));
	}
}
