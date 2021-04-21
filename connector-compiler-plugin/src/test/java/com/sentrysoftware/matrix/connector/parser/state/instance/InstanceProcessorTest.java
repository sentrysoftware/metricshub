package com.sentrysoftware.matrix.connector.parser.state.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;

class InstanceProcessorTest {

	private static final String VENDOR = "Vendor";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_VENDOR = "Enclosure.Discovery.Instance.Vendor";
	private static final String DEVICE_ID = "DeviceID";
	private static final String ENCLOSURE_COLLECT_SOURCE_4 = "%enclosure.collect.Source(4)%";
	private static final String ENCLOSURE_DISCOVERY_INSTANCETABLE = "enclosure.discovery.instancetable";
	private static final String INSTANCE_TABLE_COLUMN_1 = "InstanceTable.Column(1)";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID2 = "enclosure.discovery.instance.deviceID";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1 = "Enclosure.Discovery.Instance.DeviceID";
	private static InstanceProcessor processor = new InstanceProcessor();

	@Test
	void testDetect() {

		final Connector connector = new Connector();
		assertTrue(processor.detect(ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1, INSTANCE_TABLE_COLUMN_1, connector));
		assertTrue(processor.detect(ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID2, INSTANCE_TABLE_COLUMN_1, connector));
		assertFalse(processor.detect(ENCLOSURE_DISCOVERY_INSTANCETABLE, ENCLOSURE_COLLECT_SOURCE_4, connector));
		assertFalse(processor.detect(null, INSTANCE_TABLE_COLUMN_1, connector));
		assertFalse(processor.detect(ENCLOSURE_DISCOVERY_INSTANCETABLE, null, connector));

	}

	@Test
	void testParse() {
		final Connector connector = new Connector();
		final Discovery discovery = Discovery.builder().build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
				.discovery(discovery).build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		processor.parse(ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1, INSTANCE_TABLE_COLUMN_1, connector);
		assertEquals(INSTANCE_TABLE_COLUMN_1,
				connector.getHardwareMonitors().get(0).getDiscovery().getParameters().get(DEVICE_ID));
	}

	@Test
	void testGetParameter() {
		assertEquals(DEVICE_ID, processor.getParameter(ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1));
		assertEquals(VENDOR, processor.getParameter(ENCLOSURE_DISCOVERY_INSTANCE_VENDOR));
	}

}
