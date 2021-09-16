package com.sentrysoftware.matrix.connector.model.monitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public enum MonitorType {

	CONNECTOR("Connector", new MetaConnector(), "connector"),
	TARGET("Target", new Target(), "target"),
	BATTERY("Battery", new Battery(), "battery"),
	BLADE("Blade", new Blade(), "blade"),
	CPU("CPU", new Cpu(), "cpu"),
	CPU_CORE("CpuCore", new CpuCore(), "cpucore"),
	DISK_CONTROLLER("DiskController", new DiskController(), "diskcontroller"),
	ENCLOSURE("Enclosure", new Enclosure(), "enclosure"),
	FAN("Fan", new Fan(), "fan"),
	LED("LED", new Led(), "led"),
	LOGICAL_DISK("LogicalDisk", new LogicalDisk(), "logicaldisk"),
	LUN("Lun", new Lun(), "lun"),
	MEMORY("Memory", new Memory(), "memory"),
	NETWORK_CARD("NetworkCard", new NetworkCard(), "networkcard"),
	OTHER_DEVICE("OtherDevice", new OtherDevice(), "otherdevice"),
	PHYSICAL_DISK("PhysicalDisk", new PhysicalDisk(), "physicaldisk"),
	POWER_SUPPLY("PowerSupply", new PowerSupply(), "powersupply"),
	ROBOTICS("Robotic", new Robotics(), "robotics"),
	TAPE_DRIVE("TapeDrive", new TapeDrive(), "tapedrive"),
	TEMPERATURE("Temperature", new Temperature(), "temperature"),
	VOLTAGE("Voltage", new Voltage(), "voltage");

	public static final List<MonitorType> MONITOR_TYPES = Collections.unmodifiableList(Arrays.asList(MonitorType.values()));

	private String name;
	private IMetaMonitor metaMonitor;
	private String key;

	/**
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link MonitorType} instance
	 */
	public static MonitorType getByName(@NonNull final String name) {
		return MONITOR_TYPES.stream().filter(n -> name.equalsIgnoreCase(n.getName())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined monitor name: " + name));
	}

	/**
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link Optional} of {@link MonitorType} instance
	 */
	public static Optional<MonitorType> getByNameOptional(final String name) {
		if (name == null) {
			return Optional.empty();
		}
		return MONITOR_TYPES.stream().filter(n -> name.equalsIgnoreCase(n.getName())).findFirst();
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
