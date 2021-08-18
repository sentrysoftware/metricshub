package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Arrays;
import java.util.List;
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
	void testBuildGenericName() {

		assertEquals("display 12345", MonitorNameBuilder.buildGenericName("dev 12345", "display 12345", "0",
					Arrays.asList("dev", "device")));
		assertEquals("12345", MonitorNameBuilder.buildGenericName("dev 12345", " ", "0",
				Arrays.asList("dev", "device")));
		assertEquals("0", MonitorNameBuilder.buildGenericName("dev 12345678901234567890", " ", "0",
				Arrays.asList("dev", "device")));
		assertEquals("0", MonitorNameBuilder.buildGenericName(" ", " ", "0",
				Arrays.asList("dev", "device")));
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
					.monitorType(MonitorType.FAN)
					.monitor(monitor)
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
			
			assertEquals("1.1 (Intel CMOS - System Board CMOS Battery)", MonitorNameBuilder.buildBatteryName(buildingInfo));
		}
	}

}
