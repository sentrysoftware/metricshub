package com.sentrysoftware.matrix.connector.model.monitor;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MonitorType {

	CONNECTOR("Connector", new ConcreteConnector()),
	TARGET("Target", new Target()),
	BATTERY("Battery", new Battery()),
	BLADE("Blade", new Blade()),
	CPU("CPU", new Cpu()),
	CPU_CORE("CpuCore", new CpuCore()),
	DISK_CONTROLLER("DiskController", new DiskController()),
	DISK_ENCLOSURE("DiskEnclosure", new DiskEnclosure()),
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
	private IMonitorConcreteType concreteType;

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

	public static interface IMonitorConcreteType {
		public void accept(IMonitorVisitor monitorVisitor);
	}

	public static class ConcreteConnector implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Target implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Battery implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Blade implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Cpu implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class CpuCore implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class DiskController implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class DiskEnclosure implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Enclosure implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Fan implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Led implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class LogicalDisk implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Lun implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Memory implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class NetworkCard implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class OtherDevice implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class PhysicalDisk implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class PowerSupply implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Robotic implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class TapeDrive implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Temperature implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}

	public static class Voltage implements IMonitorConcreteType {

		@Override
		public void accept(IMonitorVisitor monitorVisitor) {
			monitorVisitor.visit(this);

		}

	}
}
