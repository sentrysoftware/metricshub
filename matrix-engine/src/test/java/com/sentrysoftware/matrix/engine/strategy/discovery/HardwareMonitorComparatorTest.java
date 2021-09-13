package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

class HardwareMonitorComparatorTest {

	final HardwareMonitor hardwareMonitorWithoutType = HardwareMonitor.builder().build();
	final HardwareMonitor hardwareMonitorNull = null;

	final HardwareMonitor hdfConnector = HardwareMonitor.builder().type(MonitorType.CONNECTOR).build();
	final HardwareMonitor hdfTarget = HardwareMonitor.builder().type(MonitorType.TARGET).build();
	final HardwareMonitor hdfBattery = HardwareMonitor.builder().type(MonitorType.BATTERY).build();
	final HardwareMonitor hdfBlade = HardwareMonitor.builder().type(MonitorType.BLADE).build();
	final HardwareMonitor hdfCPU = HardwareMonitor.builder().type(MonitorType.CPU).build();
	final HardwareMonitor hdfCpuCore = HardwareMonitor.builder().type(MonitorType.CPU_CORE).build();
	final HardwareMonitor hdfDiskController = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).build();
	final HardwareMonitor hdfEnclosure = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).build();
	final HardwareMonitor hdfFan = HardwareMonitor.builder().type(MonitorType.FAN).build();
	final HardwareMonitor hdfLed = HardwareMonitor.builder().type(MonitorType.LED).build();
	final HardwareMonitor hdfLogicalDisk = HardwareMonitor.builder().type(MonitorType.LOGICAL_DISK).build();
	final HardwareMonitor hdfLun = HardwareMonitor.builder().type(MonitorType.LUN).build();
	final HardwareMonitor hdfMemory = HardwareMonitor.builder().type(MonitorType.MEMORY).build();
	final HardwareMonitor hdfNetworkCard = HardwareMonitor.builder().type(MonitorType.NETWORK_CARD).build();
	final HardwareMonitor hdfOtherDevice = HardwareMonitor.builder().type(MonitorType.OTHER_DEVICE).build();
	final HardwareMonitor hdfPhysicalDisk = HardwareMonitor.builder().type(MonitorType.PHYSICAL_DISK).build();
	final HardwareMonitor hdfPowerSupply = HardwareMonitor.builder().type(MonitorType.POWER_SUPPLY).build();
	final HardwareMonitor hdfRobotic = HardwareMonitor.builder().type(MonitorType.ROBOTICS).build();
	final HardwareMonitor hdfTapeDrive = HardwareMonitor.builder().type(MonitorType.TAPE_DRIVE).build();
	final HardwareMonitor hdfTemperature = HardwareMonitor.builder().type(MonitorType.TEMPERATURE).build();
	final HardwareMonitor hdfVoltage = HardwareMonitor.builder().type(MonitorType.VOLTAGE).build();

	final List<HardwareMonitor> allPossibleMonitorsType = Arrays.asList(
			hdfTarget, hdfBattery, hdfBlade, hdfCPU,
			hdfCpuCore, hdfDiskController, hdfFan, hdfEnclosure,
			hdfLed, hdfLogicalDisk, hdfLun, hdfMemory,
			hdfNetworkCard, hdfOtherDevice, hdfPhysicalDisk, hdfPowerSupply,
			hdfRobotic, hdfTapeDrive, hdfTemperature, hdfVoltage);

	@Test
	void testCompareAllMonitorTypes() {
		List<MonitorType> monitorsSorted = allPossibleMonitorsType.stream().sorted(new HardwareMonitorComparator())
				.map(HardwareMonitor::getType).collect(Collectors.toList());

		List<MonitorType> expectedSort = Arrays.asList(MonitorType.ENCLOSURE, MonitorType.BLADE,
				MonitorType.DISK_CONTROLLER, MonitorType.CPU, MonitorType.TARGET, MonitorType.BATTERY,
				MonitorType.CPU_CORE, MonitorType.FAN,
				MonitorType.LED, MonitorType.LOGICAL_DISK, MonitorType.LUN, MonitorType.MEMORY,
				MonitorType.NETWORK_CARD, MonitorType.OTHER_DEVICE, MonitorType.PHYSICAL_DISK, MonitorType.POWER_SUPPLY,
				MonitorType.ROBOTICS, MonitorType.TAPE_DRIVE, MonitorType.TEMPERATURE, MonitorType.VOLTAGE);
		
		assertEquals(expectedSort, monitorsSorted);
	}

	// here we will only select some monitors : 
	// hardwareMonitorWithoutType, ENCLOSURE, DISK_ENCLOSURE, BLADE, CPU, CPU_CORE, DISK_CONTROLLER
	// Note that the actual priorities are : ENCLOSURE, BLADE, DISK_CONTROLLER, CPU

	@Test
	void testCompareMonitorTypeTwoByTwo1(){

		assertEquals(0 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hardwareMonitorWithoutType) );
		assertEquals(0 , new HardwareMonitorComparator().compare(hdfEnclosure, hdfEnclosure));
	}

	@Test
	void testCompareMonitorTypeTwoByTwo2(){
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfEnclosure));
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorNull, hdfEnclosure));
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfEnclosure, hardwareMonitorWithoutType) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfEnclosure, hardwareMonitorNull));
	}

	@Test
	void testCompareMonitorTypeTwoByTwo3(){
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfEnclosure) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfBlade) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfCPU) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfCpuCore) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hardwareMonitorWithoutType, hdfDiskController) );
	}

	@Test
	void testCompareMonitorTypeTwoByTwo4(){
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfEnclosure, hardwareMonitorWithoutType) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfEnclosure, hdfBlade) );
		assertEquals(-3 , new HardwareMonitorComparator().compare(hdfEnclosure, hdfCPU) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfEnclosure, hdfCpuCore) );
		assertEquals(-2 , new HardwareMonitorComparator().compare(hdfEnclosure, hdfDiskController) );
	}

	@Test
	void testCompareMonitorTypeTwoByTwo6(){
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfBlade, hardwareMonitorWithoutType) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hdfBlade, hdfEnclosure) );
		assertEquals(-2 , new HardwareMonitorComparator().compare(hdfBlade, hdfCPU) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfBlade, hdfCpuCore) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfBlade, hdfDiskController) );
	}

	@Test
	void testCompareMonitorTypeTwoByTwo7(){
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfCPU, hardwareMonitorWithoutType) );
		assertEquals(3 , new HardwareMonitorComparator().compare(hdfCPU, hdfEnclosure) );
		assertEquals(2 , new HardwareMonitorComparator().compare(hdfCPU, hdfBlade) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfCPU, hdfCpuCore) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hdfCPU, hdfDiskController) );
	}

	@Test
	void testCompareMonitorTypeTwoByTwo8(){
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfDiskController, hardwareMonitorWithoutType) );
		assertEquals(2 , new HardwareMonitorComparator().compare(hdfDiskController, hdfEnclosure) );
		assertEquals(1 , new HardwareMonitorComparator().compare(hdfDiskController, hdfBlade) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfDiskController, hdfCpuCore) );
		assertEquals(-1 , new HardwareMonitorComparator().compare(hdfDiskController, hdfCPU) );
	}

	@Test
	void testCompareNullValues() {
		assertEquals(0 , new HardwareMonitorComparator().compare(null, null));
		assertEquals(0 , new HardwareMonitorComparator().compare(null, new HardwareMonitor()));
		assertEquals(0 , new HardwareMonitorComparator().compare(new HardwareMonitor(), new HardwareMonitor()));
		assertEquals(0 , new HardwareMonitorComparator().compare(new HardwareMonitor(), null));
	}
}
