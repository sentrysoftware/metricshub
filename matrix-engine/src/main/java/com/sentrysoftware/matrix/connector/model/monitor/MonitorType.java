package com.sentrysoftware.matrix.connector.model.monitor;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.util.Assert;

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
import com.sentrysoftware.matrix.common.meta.monitor.Robotic;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonitorType {

	CONNECTOR("Connector", new MetaConnector()),
	TARGET("Target", new Target()),
	BATTERY("Battery", new Battery()),
	BLADE("Blade", new Blade()),
	CPU("CPU", new Cpu()),
	CPU_CORE("CpuCore", new CpuCore()),
	DISK_CONTROLLER("DiskController", new DiskController()),
	ENCLOSURE("Enclosure", new Enclosure()),
	FAN("Fan", new Fan()),
	LED("LED", new Led()),
	LOGICAL_DISK("LogicalDisk", new LogicalDisk()),
	LUN("Lun", new Lun()),
	MEMORY("Memory", new Memory()),
	NETWORK_CARD("NetworkCard", new NetworkCard()),
	OTHER_DEVICE("OtherDevice", new OtherDevice()),
	PHYSICAL_DISK("PhysicalDisk", new PhysicalDisk()),
	POWER_SUPPLY("PowerSupply", new PowerSupply()),
	ROBOTIC("Robotic", new Robotic()),
	TAPE_DRIVE("TapeDrive", new TapeDrive()),
	TEMPERATURE("Temperature", new Temperature()),
	VOLTAGE("Voltage", new Voltage());

	private String name;
	private IMetaMonitor metaMonitor;

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
	 * Get {@link MonitorType} by name, the name defined in the hardware connector code
	 * @param name
	 * @return {@link Optional} of {@link MonitorType} instance
	 */
	public static Optional<MonitorType> getByNameOptional(final String name) {
		if (name == null) {
			return Optional.empty();
		}
		return Arrays.stream(MonitorType.values()).filter(n -> name.equalsIgnoreCase(n.getName())).findFirst();
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
