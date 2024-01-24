package org.sentrysoftware.metricshub.engine.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing known monitor types.
 * Each enum constant corresponds to a specific monitor type with a unique key.
 */
@Getter
@AllArgsConstructor
public enum KnownMonitorType {
	/**
	 * Connector monitor type.
	 */
	CONNECTOR("connector"),
	/**
	 * Host monitor type.
	 */
	HOST("host"),
	/**
	 * Battery monitor type.
	 */
	BATTERY("battery"),
	/**
	 * Blade monitor type.
	 */
	BLADE("blade"),
	/**
	 * CPU monitor type.
	 */
	CPU("cpu"),
	/**
	 * Disk controller monitor type.
	 */
	DISK_CONTROLLER("disk_controller"),
	/**
	 * Enclosure monitor type.
	 */
	ENCLOSURE("enclosure"),
	/**
	 * Fan monitor type.
	 */
	FAN("fan"),
	/**
	 * GPU monitor type.
	 */
	GPU("gpu"),
	/**
	 * LED monitor type.
	 */
	LED("led"),
	/**
	 * Logical disk monitor type.
	 */
	LOGICAL_DISK("logical_disk"),
	/**
	 * LUN monitor type.
	 */
	LUN("lun"),
	/**
	 * Memory monitor type.
	 */
	MEMORY("memory"),
	/**
	 * Network monitor type.
	 */
	NETWORK("network"),
	/**
	 * Other device monitor type.
	 */
	OTHER_DEVICE("other_device"),
	/**
	 * Physical disk monitor type.
	 */
	PHYSICAL_DISK("physical_disk"),
	/**
	 * Power supply monitor type.
	 */
	POWER_SUPPLY("power_supply"),
	/**
	 * Robotics monitor type.
	 */
	ROBOTICS("robotics"),
	/**
	 * Tape drive monitor type.
	 */
	TAPE_DRIVE("tape_drive"),
	/**
	 * Temperature monitor type.
	 */
	TEMPERATURE("temperature"),
	/**
	 * Virtual machine monitor type.
	 */
	VM("vm"),
	/**
	 * Voltage monitor type.
	 */
	VOLTAGE("voltage");

	private String key;
}
