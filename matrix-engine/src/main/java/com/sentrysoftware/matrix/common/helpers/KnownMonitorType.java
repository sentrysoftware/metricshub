package com.sentrysoftware.matrix.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnownMonitorType {

	CONNECTOR("connector"),
	HOST("host"),
	BATTERY("battery"),
	BLADE("blade"),
	CPU("cpu"),
	DISK_CONTROLLER("disk_controller"),
	ENCLOSURE("enclosure"),
	FAN("fan"),
	GPU("gpu"),
	LED("led"),
	LOGICAL_DISK("logical_disk"),
	LUN("lun"),
	MEMORY("memory"),
	NETWORK("network"),
	OTHER_DEVICE("other_device"),
	PHYSICAL_DISK("physical_disk"),
	POWER_SUPPLY("power_supply"),
	ROBOTICS("robotics"),
	TAPE_DRIVE("tape_drive"),
	TEMPERATURE("temperature"),
	VM("vm"),
	VOLTAGE("voltage");

	private String key;

}

