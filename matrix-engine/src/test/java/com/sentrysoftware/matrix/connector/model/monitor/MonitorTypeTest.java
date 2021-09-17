package com.sentrysoftware.matrix.connector.model.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MonitorTypeTest {

	private static final String BATTERY_LOWER = "battery";
	private static final String BATTERY_PASCAL = "Battery";
	private static final String BATTERY = "BATTERY";
	private static final String UNKNOWN_MONITOR_TEST = "unknownMonitorTest";

	@Test
	void MonitorTypeJsonKeyTest() {
		MonitorType monitor = MonitorType.CONNECTOR;
		assertEquals("Connectors", monitor.jsonKey());

		monitor = MonitorType.TARGET;
		assertEquals("Targets", monitor.jsonKey());

		monitor = MonitorType.BATTERY;
		assertEquals("Batteries", monitor.jsonKey());

		monitor = MonitorType.BLADE;
		assertEquals("Blades", monitor.jsonKey());

		monitor = MonitorType.CPU;
		assertEquals("CPUs", monitor.jsonKey());

		monitor = MonitorType.CPU_CORE;
		assertEquals("CpuCores", monitor.jsonKey());

		monitor = MonitorType.DISK_CONTROLLER;
		assertEquals("DiskControllers", monitor.jsonKey());

		monitor = MonitorType.ENCLOSURE;
		assertEquals("Enclosures", monitor.jsonKey());

		monitor = MonitorType.FAN;
		assertEquals("Fans", monitor.jsonKey());

		monitor = MonitorType.LED;
		assertEquals("LEDs", monitor.jsonKey());

		monitor = MonitorType.LOGICAL_DISK;
		assertEquals("LogicalDisks", monitor.jsonKey());

		monitor = MonitorType.LUN;
		assertEquals("Luns", monitor.jsonKey());

		monitor = MonitorType.MEMORY;
		assertEquals("Memories", monitor.jsonKey());

		monitor = MonitorType.NETWORK_CARD;
		assertEquals("NetworkCards", monitor.jsonKey());

		monitor = MonitorType.OTHER_DEVICE;
		assertEquals("OtherDevices", monitor.jsonKey());

		monitor = MonitorType.PHYSICAL_DISK;
		assertEquals("PhysicalDisks", monitor.jsonKey());

		monitor = MonitorType.POWER_SUPPLY;
		assertEquals("PowerSupplies", monitor.jsonKey());

		monitor = MonitorType.ROBOTICS;
		assertEquals("Robotics", monitor.jsonKey());

		monitor = MonitorType.TAPE_DRIVE;
		assertEquals("TapeDrives", monitor.jsonKey());

		monitor = MonitorType.TEMPERATURE;
		assertEquals("Temperatures", monitor.jsonKey());

		monitor = MonitorType.VOLTAGE;
		assertEquals("Voltages", monitor.jsonKey());
	}

	@Test
	void testGetByName() {
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnector(BATTERY_LOWER));
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnector(BATTERY_PASCAL));
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnector(BATTERY));
		assertThrows(IllegalArgumentException.class, () -> MonitorType.getByNameInConnector(null));
		assertThrows(IllegalArgumentException.class, () -> MonitorType.getByNameInConnector(UNKNOWN_MONITOR_TEST));
	}

	@Test
	void testGetByNameOptional() {
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnectorOptional(BATTERY_LOWER).get());
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnectorOptional(BATTERY_PASCAL).get());
		assertEquals(MonitorType.BATTERY, MonitorType.getByNameInConnectorOptional(BATTERY).get());
		assertTrue(MonitorType.getByNameInConnectorOptional(null).isEmpty());
		assertTrue(MonitorType.getByNameInConnectorOptional(UNKNOWN_MONITOR_TEST).isEmpty());
	}
}
