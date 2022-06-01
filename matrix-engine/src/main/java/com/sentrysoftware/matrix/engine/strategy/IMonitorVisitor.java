package com.sentrysoftware.matrix.engine.strategy;

import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
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
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;

public interface IMonitorVisitor {

	void visit(MetaConnector metaConnector);

	void visit(Host host);

	void visit(Battery battery);

	void visit(Blade blade);

	void visit(Cpu cpu);

	void visit(CpuCore cpuCore);

	void visit(DiskController diskController);

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

	void visit(Temperature temperature);

	void visit(Voltage voltage);

	void visit(Robotics robotics);

	void visit(Vm vm);

	void visit(Gpu gpu);
}
