package com.sentrysoftware.matrix.connector.model.monitor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonitorType {

	CONNECTOR("Connector"),
	DEVICE("Device"),
	BATTERY("Battery"),
	BLADE("Blade"),
	CPU("CPU"),
	CPU_CORE("CpuCore"),
	DISK_CONTROLLER("DiskController"),
	DISK_ENCLOSURE("DiskEnclosure"),
	ENCLOSURE("Enclosure"),
	FAN("Fan"),
	LED("LED"),
	LOGICAL_DISK("LogicalDisk"),
	LUN("Lun"),
	MEMORY("Memory"),
	NETWORK_CARD("NetworkCard"),
	OTHER_DEVICE("OtherDevice"),
	PHYSICAL_DISK("PhysicalDisk"),
	POWER_SUPPLY("PowerSupply"),
	ROBOTIC("Robotic"),
	TAPE_DRIVE("TapeDrive"),
	TEMPERATURE("Temperature"),
	VOLTAGE("Voltage");

	private String name;
}
