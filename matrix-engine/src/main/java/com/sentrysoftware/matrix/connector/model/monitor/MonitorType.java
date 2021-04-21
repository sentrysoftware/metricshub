package com.sentrysoftware.matrix.connector.model.monitor;

import java.util.Arrays;

import org.springframework.util.Assert;

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

	/**
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link MonitorType} instance
	 */
	public static MonitorType getByName(final String name) {
		Assert.notNull(name, "name cannot be null.");
		return Arrays.stream(MonitorType.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined monitor name: " + name));
	}

	/**
	 * Return the JSON key associated to a monitor type.
	 * @return The JSON key associated to the monitor type.
	 */
	public String jsonKey() {
		switch(this) {
			case BATTERY:
			case MEMORY:
			case POWER_SUPPLY:
				return name.replace("y", "ies");
			default:
				return name.concat("s");
		}
	}
}
