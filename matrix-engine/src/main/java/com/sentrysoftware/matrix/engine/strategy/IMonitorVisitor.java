package com.sentrysoftware.matrix.engine.strategy;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Battery;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Blade;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ConcreteConnector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Cpu;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.CpuCore;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Target;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DiskController;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DiskEnclosure;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Enclosure;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Fan;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Led;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.LogicalDisk;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Lun;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Memory;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.NetworkCard;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.OtherDevice;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.PhysicalDisk;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.PowerSupply;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Robotic;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TapeDrive;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Temperature;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType.Voltage;

public interface IMonitorVisitor {

	void visit(ConcreteConnector concreteConnector);

	void visit(Target device);

	void visit(Battery battery);

	void visit(Blade blade);

	void visit(Cpu cpu);

	void visit(CpuCore cpuCore);

	void visit(DiskController diskController);

	void visit(DiskEnclosure diskEnclosure);

	void visit(Enclosure enclosure);

	void visit(Fan fan);

	void visit(Led led);

	void visit(LogicalDisk logicalDisk);

	void visit(Lun lun);

	void visit(Memory memory);

	void visit(NetworkCard networkCard);

	void visit(OtherDevice otherDevice);

	void visit(PhysicalDisk physicalDisk);

	void visit(PowerSupply powerSupply);

	void visit(TapeDrive tapeDrive);

	void visit(Temperature concreteTemperature);

	void visit(Voltage concreteVoltage);

	void visit(Robotic concreteRobotic);

	
}
