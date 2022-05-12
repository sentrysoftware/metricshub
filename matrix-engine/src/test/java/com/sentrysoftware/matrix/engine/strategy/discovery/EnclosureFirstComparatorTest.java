package com.sentrysoftware.matrix.engine.strategy.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

class EnclosureFirstComparatorTest {

	private static final String EMPTY = "";
	final SNMPGetTableSource source1 = SNMPGetTableSource.builder().oid("oid1").key("key1").computes(Collections.singletonList(LeftConcat.builder().column(1).string(EMPTY).build())).build();
	final SNMPGetTableSource source2 = SNMPGetTableSource.builder().oid("oid2").key("key2").computes(Collections.singletonList(RightConcat.builder().column(1).string(EMPTY).build())).build();

	final HardwareMonitor hdfConnector = HardwareMonitor.builder().type(MonitorType.CONNECTOR).build();
	final HardwareMonitor hdfTarget = HardwareMonitor.builder().type(MonitorType.TARGET).build();
	final HardwareMonitor hdfBattery = HardwareMonitor.builder().type(MonitorType.BATTERY).build();
	final HardwareMonitor hdfBlade = HardwareMonitor.builder().type(MonitorType.BLADE).build();
	final HardwareMonitor hdfCPU = HardwareMonitor.builder().type(MonitorType.CPU).build();
	final HardwareMonitor hdfCpuCore = HardwareMonitor.builder().type(MonitorType.CPU_CORE).build();
	final HardwareMonitor hdfDiskController = HardwareMonitor.builder().type(MonitorType.DISK_CONTROLLER).build();
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

	final List<HardwareMonitor> allPossibleConnectorsType = Arrays.asList(
			hdfTarget, hdfBattery, hdfBlade, hdfCPU,
			hdfCpuCore, hdfDiskController, hdfFan,
			hdfLed, hdfLogicalDisk, hdfLun, hdfMemory,
			hdfNetworkCard, hdfOtherDevice, hdfPhysicalDisk, hdfPowerSupply,
			hdfRobotic, hdfTapeDrive, hdfTemperature, hdfVoltage);

	final List<HardwareMonitor> withoutEnclosure = Arrays.asList(
			hdfTarget, hdfBattery, hdfBlade, hdfCPU,
			hdfCpuCore, hdfDiskController, hdfFan,
			hdfLed, hdfLogicalDisk, hdfLun, hdfMemory, hdfNetworkCard,
			hdfOtherDevice, hdfPhysicalDisk, hdfPowerSupply, hdfRobotic,
			hdfTapeDrive, hdfTemperature, hdfVoltage);

	final HardwareMonitor hdfEmptyEnclosure = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).build();
	final HardwareMonitor hdfEnclosureDiscoveryOnly = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(new Discovery())
			.build();
	final HardwareMonitor hdfEnclosureDiscoverySource = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(Discovery
					.builder()
					.sources(Arrays.asList(source1, source2))
					.build())
			.build();
	final HardwareMonitor hdfEnclosureDiscoveryEmptySource = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(Discovery
					.builder()
					.sources(Collections.emptyList())
					.build())
			.build();
	final HardwareMonitor hdfFanDiscoverySource = HardwareMonitor
			.builder()
			.type(MonitorType.FAN)
			.discovery(Discovery
					.builder()
					.sources(Arrays.asList(source1, source2)).build())
			.build();
	final HardwareMonitor hdfEnclosureComputer = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(Discovery
					.builder()
					.parameters(Map.of("type", "computer"))
					.sources(Collections.emptyList()).build())
			.build();
	final HardwareMonitor hdfEnclosureStorage = HardwareMonitor
			.builder()
			.type(MonitorType.ENCLOSURE)
			.discovery(Discovery
					.builder()
					.parameters(Map.of("type", "storage"))
					.sources(Collections.emptyList()).build())
			.build();
	final Connector connectorWithoutMonitor = Connector.builder().compiledFilename("hdf1").build();
	final Connector connectorWithAllDevicesEmpty  = Connector.builder().compiledFilename("hdf2")
			.hardwareMonitors(allPossibleConnectorsType)
			.build();

	final Connector enclosureDiscoveryOnlyWith2Devices  = Connector.builder().compiledFilename("hdf3")
			.hardwareMonitors(Arrays.asList(hdfTarget, hdfBattery, hdfEnclosureDiscoveryOnly))
			.build();

	final Connector enclosureFullWith2Devices  = Connector.builder().compiledFilename("hdf4")
			.hardwareMonitors(Arrays.asList(hdfTemperature, hdfVoltage, hdfEnclosureDiscoverySource))
			.build();
	
	final Connector connectorWithAllPossibleEnclosures  = Connector.builder().compiledFilename("hdf5")
			.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoverySource, hdfEnclosureDiscoveryOnly, hdfEmptyEnclosure, hdfEnclosureDiscoveryEmptySource))
			.build();
	final Connector connectorWithFanOnly  = Connector.builder().compiledFilename("hdf6")
			.hardwareMonitors(Arrays.asList(hdfFanDiscoverySource))
			.build();

	final Connector connectorWithEnclosuresDiscoveryOnly  = Connector.builder().compiledFilename("hdf7")
			.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoveryOnly, hdfFanDiscoverySource, hdfLogicalDisk))
			.build();
	final Connector connectorWithEnclosuresDiscoverySource  = Connector.builder().compiledFilename("hdf8")
			.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoverySource, hdfFanDiscoverySource, hdfLogicalDisk))
			.build();
	final Connector connectorWithEmptyEnclosure  = Connector.builder().compiledFilename("hdf10")
			.hardwareMonitors(Arrays.asList(hdfEmptyEnclosure, hdfFanDiscoverySource, hdfLogicalDisk))
			.build();
	final Connector connectorWithEnclosuresOnly  = Connector.builder().compiledFilename("hdf11")
			.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoveryEmptySource, hdfFanDiscoverySource, hdfLogicalDisk))
			.build();

	@Test
	void testCompareDifferentMonitorTypes() {

		List<String> connectorsSorted = Arrays
				.asList(connectorWithoutMonitor, connectorWithAllDevicesEmpty, enclosureDiscoveryOnlyWith2Devices,
						enclosureFullWith2Devices, connectorWithAllPossibleEnclosures,
						connectorWithEnclosuresDiscoveryOnly, connectorWithEnclosuresDiscoverySource,
						connectorWithEmptyEnclosure, connectorWithEnclosuresOnly, connectorWithFanOnly)
				.stream().sorted(new EnclosureFirstComparator()).map(Connector::getCompiledFilename)
				.collect(Collectors.toList());
		assertEquals(Arrays.asList("hdf11", "hdf3", "hdf4", "hdf5", "hdf7", "hdf8", "hdf1", "hdf2", "hdf10", "hdf6"), connectorsSorted);

		connectorsSorted = Arrays
				.asList(connectorWithoutMonitor, connectorWithAllDevicesEmpty, enclosureDiscoveryOnlyWith2Devices,
						connectorWithAllPossibleEnclosures,
						connectorWithEnclosuresDiscoveryOnly, connectorWithEnclosuresDiscoverySource,
						connectorWithEmptyEnclosure, connectorWithEnclosuresOnly, connectorWithFanOnly)
				.stream().sorted(new EnclosureFirstComparator()).map(Connector::getCompiledFilename)
				.collect(Collectors.toList());
		assertEquals(Arrays.asList("hdf11", "hdf3", "hdf5", "hdf7", "hdf8", "hdf1", "hdf2", "hdf10", "hdf6"), connectorsSorted);
		
	}

	@Test
	void testCompareEnclosures() {	

		final Connector emptyEnclosure = Connector.builder().compiledFilename("hdf1")
				.hardwareMonitors(Arrays.asList(hdfEmptyEnclosure)).build();
		final Connector enclosureDiscovery = Connector.builder().compiledFilename("hdf2")
				.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoveryOnly)).build();
		final Connector enclosureDiscoverySource = Connector.builder().compiledFilename("hdf4")
				.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoverySource)).build();
		final Connector enclosureDiscoveryEmptySource = Connector.builder().compiledFilename("hdf5")
				.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoveryEmptySource)).build();
		final Connector connectorFanDiscoverySource = Connector.builder().compiledFilename("hdf6")
				.hardwareMonitors(Arrays.asList(hdfFanDiscoverySource)).build();

		assertEquals(1 , new EnclosureFirstComparator().compare(emptyEnclosure, enclosureDiscovery) );
		assertEquals(1 , new EnclosureFirstComparator().compare(emptyEnclosure, enclosureDiscoverySource) );
		assertEquals(1 , new EnclosureFirstComparator().compare(emptyEnclosure, enclosureDiscoveryEmptySource) );
		assertEquals(1 , new EnclosureFirstComparator().compare(emptyEnclosure, connectorFanDiscoverySource) );

		assertEquals(-1 , new EnclosureFirstComparator().compare(enclosureDiscovery, emptyEnclosure) );
		assertEquals(-2 , new EnclosureFirstComparator().compare(enclosureDiscovery, enclosureDiscoverySource) );
		assertEquals(-3 , new EnclosureFirstComparator().compare(enclosureDiscovery, enclosureDiscoveryEmptySource) );
		assertEquals(-1 , new EnclosureFirstComparator().compare(enclosureDiscovery, connectorFanDiscoverySource) );

		assertEquals(-1 , new EnclosureFirstComparator().compare(enclosureDiscoverySource, emptyEnclosure) );
		assertEquals(2 , new EnclosureFirstComparator().compare(enclosureDiscoverySource, enclosureDiscovery) );
		assertEquals(-1 , new EnclosureFirstComparator().compare(enclosureDiscoverySource, enclosureDiscoveryEmptySource) );
		assertEquals(-1 , new EnclosureFirstComparator().compare(enclosureDiscoverySource, connectorFanDiscoverySource) );

		assertEquals(1 , new EnclosureFirstComparator().compare(connectorFanDiscoverySource, emptyEnclosure) );
		assertEquals(1 , new EnclosureFirstComparator().compare(connectorFanDiscoverySource, enclosureDiscovery) );
		assertEquals(1 , new EnclosureFirstComparator().compare(connectorFanDiscoverySource, enclosureDiscoveryEmptySource) );
		assertEquals(1 , new EnclosureFirstComparator().compare(connectorFanDiscoverySource, enclosureDiscoverySource) );

		final Connector connector = Connector.builder().build();
		connector.setHardwareMonitors(null);
		assertEquals(1, new EnclosureFirstComparator().compare(connector, null));
		assertEquals(1, new EnclosureFirstComparator().compare(null, connector));
		assertEquals(1, new EnclosureFirstComparator().compare(null, null));
	}

	@Test
	void testCompareComputerEnclosures() {

		final Connector connectorWithEnclosure = Connector.builder().compiledFilename("hdf1")
				.hardwareMonitors(Arrays.asList(hdfEnclosureDiscoverySource)).build();

		final Connector connectorWithEnclosureComputer = Connector.builder().compiledFilename("hdf2")
				.hardwareMonitors(Arrays.asList(hdfEnclosureComputer)).build();

		final Connector connectorWithEnclosureStorage = Connector.builder().compiledFilename("hdf3")
				.hardwareMonitors(Arrays.asList(hdfEnclosureStorage)).build();

		// connectorWithFanOnly is named hdf6
		final List<Connector> connectorList = Arrays.asList(connectorWithEnclosure,
				connectorWithEnclosureComputer, 
				connectorWithEnclosureStorage,
				connectorWithFanOnly);
		
		final List<String> actual = connectorList.stream()
		.sorted(new EnclosureFirstComparator())
		.map(Connector::getCompiledFilename)
		.collect(Collectors.toList());

		final List<String> expected = Arrays.asList("hdf2", "hdf1", "hdf3", "hdf6");

		assertEquals(expected, actual);
	}
}
