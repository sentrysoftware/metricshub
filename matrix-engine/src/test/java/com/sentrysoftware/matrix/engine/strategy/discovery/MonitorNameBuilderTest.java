package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

class MonitorNameBuilderTest {


	@Test
	void testHandleComputerDisplayName() {

		{
			final Monitor target = Monitor.builder()
					.metadata(Map.of(HardwareConstants.LOCATION, HardwareConstants.REMOTE)).build();

			assertEquals(MonitorNameBuilder.WINDOWS_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.MS_WINDOWS));
			assertEquals(MonitorNameBuilder.LINUX_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.LINUX));
			assertEquals(MonitorNameBuilder.HP_TRU64_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.HP_TRU64_UNIX));
			assertEquals(MonitorNameBuilder.HP_OPEN_VMS_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.HP_OPEN_VMS));
			assertEquals(MonitorNameBuilder.UNKNOWN_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.STORAGE));
			assertEquals(MonitorNameBuilder.UNKNOWN_COMPUTER,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.NETWORK_SWITCH));
		}

		{
			final Monitor target = Monitor.builder()
					.metadata(Map.of(HardwareConstants.LOCATION, HardwareConstants.LOCALHOST)).build();

			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.MS_WINDOWS));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.LINUX));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.HP_TRU64_UNIX));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.HP_OPEN_VMS));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.STORAGE));
			assertEquals(MonitorNameBuilder.LOCALHOST_ENCLOSURE,
					MonitorNameBuilder.handleComputerDisplayName(target, TargetType.NETWORK_SWITCH));
		}

	}

	@Test
	void testIsLocalhost() {

		assertFalse(MonitorNameBuilder.isLocalhost(null));
		assertFalse(MonitorNameBuilder.isLocalhost(Collections.emptyMap()));
		assertFalse(MonitorNameBuilder.isLocalhost(Map.of(HardwareConstants.LOCATION, HardwareConstants.REMOTE)));
		assertTrue(MonitorNameBuilder.isLocalhost(Map.of(HardwareConstants.LOCATION, HardwareConstants.LOCALHOST)));

	}
	
	@Test
	void testBuildGenericName() {

		assertEquals("type A: display 12345",
				MonitorNameBuilder.buildName("type A", "display 12345", "dev 12345", "0", "dev(ice)*", ""));
		assertEquals("12345",
				MonitorNameBuilder.buildName("", " ", "dev device 12345", "0", "dev(ice)*", ""));
		assertEquals("0 (info)", MonitorNameBuilder.buildName("", " ", "device 12345678901234567890", "0", "dev(ice)*", "info"));
		assertEquals("type Z: 0 (info)", MonitorNameBuilder.buildName("type Z", " ", " ", "0", "dev(ice)*", "info"));
	}
	
	@Test
	void testBuildBatteryName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " battery,  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.BATTERY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildBatteryName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " battery,  1,1 ");
			metadata.put(HardwareConstants.DISPLAY_ID, "1.1");
			metadata.put(HardwareConstants.VENDOR, "Intel");
			metadata.put(HardwareConstants.MODEL, "CMOS");
			metadata.put(HardwareConstants.TYPE, "System Board CMOS Battery");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.BATTERY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " blade,  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.BLADE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildBladeName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " blade,  1,1 ");
			metadata.put(HardwareConstants.DISPLAY_ID, "1.1");
			metadata.put(HardwareConstants.BLADE_NAME, "Blade 123");
			metadata.put(HardwareConstants.MODEL, "model 1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.BLADE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "CPU1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "CPU #1");
			metadata.put(HardwareConstants.VENDOR, "Intel");
			metadata.put(HardwareConstants.MODEL, "Xeon");
			metadata.put(HardwareConstants.MAXIMUM_SPEED, "3600");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.CPU)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("CPU #1 (Intel - Xeon - 3.60 GHz)", MonitorNameBuilder.buildCpuName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "CPU1,1");
			metadata.put(HardwareConstants.VENDOR, "Intel");
			metadata.put(HardwareConstants.MAXIMUM_SPEED, "999");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.CPU)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " core  1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.CPU_CORE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "1,1");
			metadata.put(HardwareConstants.DISPLAY_ID, "1.1");
			metadata.put(HardwareConstants.VENDOR, "vendor X");
			metadata.put(HardwareConstants.MODEL, "model 1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.DISK_CONTROLLER)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.VENDOR, "Dell");
			metadata.put(HardwareConstants.MODEL, "2200");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: Dell 2200", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: model with vendor & display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(HardwareConstants.VENDOR, "Dell");
			metadata.put(HardwareConstants.MODEL, "2200 Dell");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (2200 Dell)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no model
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(HardwareConstants.VENDOR, "Dell");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (Dell)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no vendor
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(HardwareConstants.MODEL, "2200");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (2200)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Computer: no model, no vendor
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.COMPUTER);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE).monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Computer: PowerEdge 54dsf (Linux computer)",
					MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Storage: no model, no vendor, no display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: 1.1", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}
		
		// Storage: no model, no vendor, no display ID, and device ID too long
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "12345678901234567890");
			metadata.put(HardwareConstants.ID_COUNT, "1");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: 1", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Storage: no model, no vendor, but with display ID
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DISPLAY_ID, "PowerEdge 54dsf");
			metadata.put(HardwareConstants.TYPE, HardwareConstants.STORAGE);

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Storage: PowerEdge 54dsf", MonitorNameBuilder.buildEnclosureName(buildingInfo));
		}

		// Other
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

			metadata.put(HardwareConstants.DEVICE_ID, "1.1");
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.TYPE, "Other");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ENCLOSURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "FAN1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "Fan 1A 1.1 XYZ");
			metadata.put(HardwareConstants.FAN_TYPE, "1A");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.FAN).monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("Fan 1A 1.1 XYZ (1A)", MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "FAN1.1");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("1.1", MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.FAN_TYPE, "1A");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "LED1.1");
			metadata.put(HardwareConstants.DISPLAY_ID, "LED #1");
			metadata.put(HardwareConstants.NAME, "Network");
			metadata.put(HardwareConstants.COLOR, "RED");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LED)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("LED #1 (Red - Network)", MonitorNameBuilder.buildLedName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "LED1,1");
			metadata.put(HardwareConstants.COLOR, "green");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LED)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "disk01");
			metadata.put(HardwareConstants.DISPLAY_ID, "disk-01");
			metadata.put(HardwareConstants.SIZE, "10995116277760");
			metadata.put(HardwareConstants.RAID_LEVEL, "5");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LOGICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("disk-01 (RAID 5 - 10 TB)", MonitorNameBuilder.buildLogicalDiskName(buildingInfo));
		}
		
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "disk01");
			metadata.put(HardwareConstants.SIZE, "1073741824");
			metadata.put(HardwareConstants.RAID_LEVEL, "Raid 2");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LOGICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01 (Raid 2 - 1 GB)", MonitorNameBuilder.buildLogicalDiskName(buildingInfo));
		}
	}
	
	@Test
	void testBuildLunName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " LUN,1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LUN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildLunName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " LUN,  1 ");
			metadata.put(HardwareConstants.DISPLAY_ID, "LUN 1");
			metadata.put(HardwareConstants.LOCAL_DEVICE_NAME, "local 123");
			metadata.put(HardwareConstants.REMOTE_DEVICE_NAME, "remote 123");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.LUN)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " memory module 11 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.MEMORY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildMemoryName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "0");
			metadata.put(HardwareConstants.VENDOR, "HP USA");
			metadata.put(HardwareConstants.TYPE, "19");
			metadata.put(HardwareConstants.SIZE, "500");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.MEMORY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("0 (HP USA - 19 - 500 MB)", MonitorNameBuilder.buildMemoryName(buildingInfo));
		}
	}
	
	@Test
	void testBuildNetworkCardName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "network 11");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.NETWORK_CARD)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("11", MonitorNameBuilder.buildNetworkCardName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "0");
			metadata.put(HardwareConstants.VENDOR, "HP Ethernet Controller Interface 10/100 base-t");
			metadata.put(HardwareConstants.TYPE, "NIC");
			metadata.put(HardwareConstants.MODEL, "1234");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.NETWORK_CARD)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "other device 11");
			metadata.put(HardwareConstants.ADDITIONAL_LABEL, "additional details");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.OTHER_DEVICE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("0 (additional details)", MonitorNameBuilder.buildOtherDeviceName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "01");
			metadata.put(HardwareConstants.TYPE, "type C");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.OTHER_DEVICE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "disk01");
			metadata.put(HardwareConstants.DISPLAY_ID, "disk-01");
			metadata.put(HardwareConstants.SIZE, "1000000000000");
			metadata.put(HardwareConstants.VENDOR, "HP");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.PHYSICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("disk-01 (HP - 1 TB)", MonitorNameBuilder.buildPhysicalDiskName(buildingInfo));
		}
		
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "disk01");
			metadata.put(HardwareConstants.SIZE, "1000000000");
			metadata.put(HardwareConstants.VENDOR, "HP");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.PHYSICAL_DISK)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01 (HP - 1 GB)", MonitorNameBuilder.buildPhysicalDiskName(buildingInfo));
		}
	}
	
	@Test
	void testBuildPowerSupplyName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "power01");
			metadata.put(HardwareConstants.POWER_SUPPLY_POWER, "1000");
			metadata.put(HardwareConstants.POWER_SUPPLY_TYPE, "Dell");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.POWER_SUPPLY)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " robotics,  1,1,1 ");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ROBOTIC)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("111", MonitorNameBuilder.buildRoboticsName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, " robotics,  1,1,1 ");
			metadata.put(HardwareConstants.DISPLAY_ID, "1.1.1");
			metadata.put(HardwareConstants.VENDOR, "Quantum");
			metadata.put(HardwareConstants.MODEL, "Quantum 123");
			metadata.put(HardwareConstants.ROBOTIC_TYPE, "Tape Library");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.ROBOTIC)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "tape drive, 01");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.TAPE_DRIVE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("01", MonitorNameBuilder.buildTapeDriveName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "drive 12");
			metadata.put(HardwareConstants.DISPLAY_ID, "drive 12");
			metadata.put(HardwareConstants.VENDOR, "Quantum");
			metadata.put(HardwareConstants.MODEL, "Quantum 123");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.TAPE_DRIVE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "temp sensor 101");
			metadata.put(HardwareConstants.TEMPERATURE_TYPE, "fan temperature");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.TEMPERATURE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
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
			metadata.put(HardwareConstants.ID_COUNT, "0");
			metadata.put(HardwareConstants.DEVICE_ID, "voltage 101");
			metadata.put(HardwareConstants.VOLTAGE_TYPE, "fan voltage");

			final Monitor monitor = Monitor
					.builder()
					.metadata(metadata)
					.build();

			final MonitorBuildingInfo buildingInfo = MonitorBuildingInfo
					.builder()
					.connectorName("myConnector.connector")
					.monitorType(MonitorType.VOLTAGE)
					.monitor(monitor)
					.hostMonitoring(new HostMonitoring())
					.targetType(TargetType.LINUX)
					.targetMonitor(new Monitor())
					.hostname("ecs1-01")
					.build();

			assertEquals("101 (fan voltage)",
					MonitorNameBuilder.buildVoltageName(buildingInfo));
		}
	}
	
}
