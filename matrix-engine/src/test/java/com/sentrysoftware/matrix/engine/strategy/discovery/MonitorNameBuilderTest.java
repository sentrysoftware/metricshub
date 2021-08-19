package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
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

		assertEquals("display 12345",
				MonitorNameBuilder.buildGenericName("dev 12345", "display 12345", "0", Arrays.asList("dev", "device")));
		assertEquals("12345",
				MonitorNameBuilder.buildGenericName("dev 12345", " ", "0", Arrays.asList("dev", "device")));
		assertEquals("0", MonitorNameBuilder.buildGenericName("dev 12345678901234567890", " ", "0",
				Arrays.asList("dev", "device")));
		assertEquals("0", MonitorNameBuilder.buildGenericName(" ", " ", "0", Arrays.asList("dev", "device")));
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
					.monitorType(MonitorType.BATTERY)
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

		// Storage: no model, no vendor
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

			assertEquals("Storage: (0)", MonitorNameBuilder.buildEnclosureName(buildingInfo));
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

			assertEquals("Enclosure:", MonitorNameBuilder.buildEnclosureName(buildingInfo));
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

}
