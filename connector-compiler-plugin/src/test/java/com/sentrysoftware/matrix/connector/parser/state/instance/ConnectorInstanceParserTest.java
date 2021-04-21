package com.sentrysoftware.matrix.connector.parser.state.instance;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;

class ConnectorInstanceParserTest {

	private static final String ENCLOSURE_DISCOVERY_SOURCE_3 = "%Enclosure.Discovery.Source(3)%";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_DEVICEID = "enclosure.discovery.instance.deviceid";
	private static final String ENCLOSURE_DISCOVERY_INSTANCETABLE = "enclosure.discovery.instancetable";
	private static final String FAN_DISCOVERY_INSTANCE_DEVICEID = "fan.discovery.instance.deviceid";
	private static final String DEVICE_ID = "DeviceID";
	private static final String INSTANCE_TABLE_COLUMN_1 = "InstanceTable.Column(1)";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1 = "Enclosure.Discovery.Instance.DeviceID";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_TABLE = "Enclosure.Discovery.InstanceTable";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_2 = "%Enclosure.Discovery.Source(2)%";
	private static final String OID = "1.2.3.4.5.6";
	private static final ConnectorInstanceParser parser = new ConnectorInstanceParser();

	@Test
	void testDetect() {

		Connector connector = new Connector();
		assertTrue(parser.detect(FAN_DISCOVERY_INSTANCE_DEVICEID, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertTrue(parser.detect(ENCLOSURE_DISCOVERY_INSTANCE_TABLE, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertTrue(parser.detect(ENCLOSURE_DISCOVERY_INSTANCETABLE, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertTrue(parser.detect(ENCLOSURE_DISCOVERY_INSTANCE_DEVICEID, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertFalse(parser.detect(null, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertFalse(parser.detect(ENCLOSURE_DISCOVERY_INSTANCE_DEVICEID, null, connector));
	}

	@Test
	void testParse() {
		{
			final Connector connector = new Connector();
			final Discovery discovery = Discovery.builder().build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(discovery).build();
			connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

			parser.parse(ENCLOSURE_DISCOVERY_INSTANCE_DEVICE_ID1, INSTANCE_TABLE_COLUMN_1, connector);
			assertEquals(INSTANCE_TABLE_COLUMN_1,
					connector.getHardwareMonitors().get(0).getDiscovery().getParameters().get(DEVICE_ID));
		}

		{
			final Connector connector = new Connector();
			final Source sourceDiscovery1 = SNMPGetTableSource.builder().index(1).oid(OID).build();
			final Source sourceDiscovery2 = TableJoinSource.builder().index(2).build();
			final Discovery discovery = Discovery.builder().sources(Arrays.asList(sourceDiscovery1, sourceDiscovery2))
					.build();
			final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
					.discovery(discovery).build();
			connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

			parser.parse(ENCLOSURE_DISCOVERY_INSTANCE_TABLE, ENCLOSURE_DISCOVERY_SOURCE_2, connector);

			assertEquals(SourceInstanceTable.builder().source(sourceDiscovery2).build(), connector.getHardwareMonitors().get(0).getDiscovery().getInstanceTable());
		}
	}
}
