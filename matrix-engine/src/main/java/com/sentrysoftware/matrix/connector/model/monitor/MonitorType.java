package com.sentrysoftware.matrix.connector.model.monitor;

import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum MonitorType {

	CONNECTOR("Connector", "Connector", "Connectors", new MetaConnector(), "connector"),
	HOST("Host", "Host", "Hosts", new Host(), "host"),
	BATTERY("Battery", "Battery", "Batteries", new Battery(), "battery"),
	BLADE("Blade", "Blade", "Blades", new Blade(), "blade"),
	CPU("CPU", "CPU", "CPUs", new Cpu(), "cpu"),
	CPU_CORE("CpuCore", "CPU Core", "CPU Cores", new CpuCore(), "cpucore"),
	DISK_CONTROLLER("DiskController", "Disk Controller", "Disk Controllers", new DiskController(), "diskcontroller"),
	ENCLOSURE("Enclosure", "Enclosure", "Enclosures", new Enclosure(), "enclosure"),
	FAN("Fan", "Fan", "Fans", new Fan(), "fan"),
	GPU("GPU", "GPU", "GPUs", new Gpu(), "gpu"),
	LED("LED", "LED", "LEDs", new Led(), "led"),
	LOGICAL_DISK("LogicalDisk", "Logical Disk", "Logical Disks", new LogicalDisk(), "logicaldisk"),
	LUN("Lun", "LUN", "LUNs", new Lun(), "lun"),
	MEMORY("Memory", "Memory Module", "Memory Modules", new Memory(), "memory"),
	NETWORK_CARD("NetworkCard", "Network Card", "Network Cards", new NetworkCard(), "networkcard"),
	OTHER_DEVICE("OtherDevice", "Other", "Other Devices", new OtherDevice(), "otherdevice"),
	PHYSICAL_DISK("PhysicalDisk", "Physical Disk", "Physical Disks", new PhysicalDisk(), "physicaldisk"),
	POWER_SUPPLY("PowerSupply", "Power Supply", "Power Supplies", new PowerSupply(), "powersupply"),
	ROBOTICS("Robotic", "Robotics", "Robotics", new Robotics(), "robotics"),
	TAPE_DRIVE("TapeDrive", "Tape Drive", "Tape Drives", new TapeDrive(), "tapedrive"),
	TEMPERATURE("Temperature", "Temperature", "Temperatures", new Temperature(), "temperature"),
	VM("VM", "Virtual Machine", "Virtual Machines", new Vm(), "vm"),
	VOLTAGE("Voltage", "Voltage", "Voltages", new Voltage(), "voltage");

	public static final List<MonitorType> MONITOR_TYPES = Collections.unmodifiableList(Arrays.asList(MonitorType.values()));

	private String nameInConnector;
	private String displayName;
	private String displayNamePlural;
	private IMetaMonitor metaMonitor;
	private String key;

	/**
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link MonitorType} instance
	 */
	public static MonitorType getByNameInConnector(@NonNull final String name) {
		return MONITOR_TYPES.stream().filter(n -> name.equalsIgnoreCase(n.getNameInConnector())).findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Undefined monitor name: " + name));
	}

	/**
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link Optional} of {@link MonitorType} instance
	 */
	public static Optional<MonitorType> getByNameInConnectorOptional(final String name) {
		if (name == null) {
			return Optional.empty();
		}
		return MONITOR_TYPES.stream().filter(n -> name.equalsIgnoreCase(n.getNameInConnector())).findFirst();
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
				return nameInConnector.replace("y", "ies");
			default:
				return nameInConnector.concat("s");
		}
	}

}
