package com.sentrysoftware.matrix.connector.model.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MonitorTypeTest {

	@Test
	void MonitorTypejsonKeytest() {
		MonitorType monitor = MonitorType.CONNECTOR;
		assertEquals("Connectors", monitor.jsonKey());

		monitor = MonitorType.DEVICE;
		assertEquals("Devices", monitor.jsonKey());

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

		monitor = MonitorType.DISK_ENCLOSURE;
		assertEquals("DiskEnclosures", monitor.jsonKey());

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

		monitor = MonitorType.ROBOTIC;
		assertEquals("Robotics", monitor.jsonKey());

		monitor = MonitorType.TAPE_DRIVE;
		assertEquals("TapeDrives", monitor.jsonKey());

		monitor = MonitorType.TEMPERATURE;
		assertEquals("Temperatures", monitor.jsonKey());

		monitor = MonitorType.VOLTAGE;
		assertEquals("Voltages", monitor.jsonKey());
	}
}
