package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

class MonitorNameBuilderTest {
	
	private static final String MY_CONNECTOR_NAME = "myConnector.connector";
	private static final String ECS1_01 = "ecs1-01";
	private static final String _0 = "0";
	
	// Fan
	private static final String FAN_DEVICE_ID = "FAN1.1";
	private static final String FAN_DEVICE_ID_LONG = "12345678901";
	private static final String FAN_DISPLAY_ID = "Fan 1A 1.1 XYZ";
	private static final String FAN_TYPE = "1A";
	private static final String FAN_NAME_1 = "Fan 1A 1.1 XYZ (1A)";
	private static final String FAN_NAME_2 = "1.1";
	private static final String FAN_NAME_3 = "0 (1A)";
	private static final String FAN_NAME_4 = "0";

	@Test
	void testBuildFanName() {

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(HardwareConstants.DEVICE_ID, FAN_DEVICE_ID);
			metadata.put(HardwareConstants.DISPLAY_ID, FAN_DISPLAY_ID);
			metadata.put(HardwareConstants.FAN_TYPE, FAN_TYPE);

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
			assertEquals(FAN_NAME_1, MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(HardwareConstants.DEVICE_ID, FAN_DEVICE_ID);

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

			assertEquals(FAN_NAME_2, MonitorNameBuilder.buildFanName(buildingInfo));
		}

		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(HardwareConstants.FAN_TYPE, FAN_TYPE);

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
			assertEquals(FAN_NAME_3, MonitorNameBuilder.buildFanName(buildingInfo));
		}
		
		{
			final Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			metadata.put(HardwareConstants.ID_COUNT, _0);
			metadata.put(HardwareConstants.DEVICE_ID, FAN_DEVICE_ID_LONG);

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

			assertEquals(FAN_NAME_4, MonitorNameBuilder.buildFanName(buildingInfo));
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

}
