package com.sentrysoftware.matrix.connector.parser.state.instance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.SourceInstanceTable;	
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;

class InstanceTableProcessorTest {

	private static final String EMPTY = "";
	private static final String START_WITH_DOUBLE_QUOTES_ONLY = "\"Text";
	private static final String CONTAINS_DOUBLE_QUOTES_BUT_NOT_START_END = "Text (\"Text\") Text";
	private static final String NO_DOUBLE_QUOTES = "Text";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_DEVICEID = "enclosure.discovery.instance.deviceid";
	private static final String ENCLOSURE_DISCOVERY_INSTANCE_TABLE = "Enclosure.Discovery.InstanceTable";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_3 = "%Enclosure.Discovery.Source(3)%";
	private static final String FAN_DISCOVERY_INSTANCE_DEVICEID = "fan.discovery.instance.deviceid";
	private static final String ENCLOSURE = "enclosure";
	private static final String ENCLOSURE_DISCOVERY_INSTANCETABLE = "enclosure.discovery.instancetable";
	private static final String ENCLOSURE_COLLECT_SOURCE_1 = "%Enclosure.collect.Source(1)%";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_1 = "%Enclosure.Discovery.Source(1)%";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_2 = "%Enclosure.Discovery.Source(2)%";
	private static final String URL = "/url";
	private static final String OID = "1.2.3.4.5.6";
	private static final String END_WITH_DOUBLE_QUOTES_ONLY = "Text\"";
	private static InstanceTableProcessor processor = new InstanceTableProcessor();

	@Test
	void testDetect() {

		final Connector connector = new Connector();
		assertFalse(processor.detect(FAN_DISCOVERY_INSTANCE_DEVICEID, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertTrue(processor.detect(ENCLOSURE_DISCOVERY_INSTANCE_TABLE, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertTrue(processor.detect(ENCLOSURE_DISCOVERY_INSTANCETABLE, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertFalse(processor.detect(ENCLOSURE_DISCOVERY_INSTANCE_DEVICEID, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertFalse(processor.detect(null, ENCLOSURE_DISCOVERY_SOURCE_3, connector));
		assertFalse(processor.detect(ENCLOSURE_DISCOVERY_INSTANCE_TABLE, null, connector));
	}

	@Test
	void testParse() {
		final Connector connector = new Connector();
		final Source sourceDiscovery1 = SNMPGetTableSource.builder().index(1).oid(OID).build();
		final Source sourceDiscovery2 = TableJoinSource.builder().index(2).build();
		final Discovery discovery = Discovery.builder().sources(Arrays.asList(sourceDiscovery1, sourceDiscovery2))
				.build();
		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder().type(MonitorType.ENCLOSURE)
				.discovery(discovery).build();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		processor.parse(ENCLOSURE_DISCOVERY_INSTANCE_TABLE, ENCLOSURE_DISCOVERY_SOURCE_2, connector);

		assertEquals(SourceInstanceTable.builder().sourceKey("Enclosure.Discovery.Source(2)".toLowerCase()).build(),
				connector.getHardwareMonitors().get(0).getDiscovery().getInstanceTable());

	}

	@Test
	void testGetInstanceTableFromValue() {
		{
			final Connector connector = new Connector();
			final Source sourceDiscovery1 = SNMPGetTableSource.builder().index(1).oid(OID).build();
			final Source sourceDiscovery2 = TableJoinSource.builder().index(2).build();
			final Discovery discovery = Discovery.builder().sources(Arrays.asList(sourceDiscovery1, sourceDiscovery2))
					.build();
			final Source sourceCollect1 = HTTPSource.builder().index(1).url(URL).build();
			final Source sourceCollect2 = TableUnionSource.builder().index(2).build();
			final Collect collect = Collect.builder().sources(Arrays.asList(sourceCollect1, sourceCollect2)).build();
			connector.setHardwareMonitors(Collections.singletonList(
					HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(discovery).collect(collect).build()));

			assertEquals(SourceInstanceTable.builder().sourceKey("Enclosure.Discovery.Source(1)".toLowerCase()).build(), processor.getInstanceTableFromValue(ENCLOSURE_DISCOVERY_SOURCE_1));
			assertEquals(SourceInstanceTable.builder().sourceKey("Enclosure.collect.Source(1)".toLowerCase()).build(), processor.getInstanceTableFromValue(ENCLOSURE_COLLECT_SOURCE_1));

		}

	}

	@Test
	void testGetHardwareMonitor() {
		{
			final Connector connector = new Connector();
			final Source sourceDiscovery1 = SNMPGetTableSource.builder().index(1).oid(OID).build();
			final Source sourceDiscovery2 = TableJoinSource.builder().index(2).build();
			final Discovery discovery = Discovery.builder().sources(Arrays.asList(sourceDiscovery1, sourceDiscovery2))
					.build();
			final HardwareMonitor expected = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(discovery)
					.build();
			connector.setHardwareMonitors(Collections.singletonList(expected));

			assertEquals(expected, processor.getHardwareMonitor(ENCLOSURE_DISCOVERY_INSTANCETABLE, connector));
		}

		{
			final Connector connector = new Connector();
			final Discovery discovery = Discovery.builder().build();
			final HardwareMonitor expected = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(discovery)
					.build();
			assertEquals(expected, processor.getHardwareMonitor(ENCLOSURE_DISCOVERY_INSTANCETABLE, connector));
		}
	}

	@Test
	void testCreateHardwareMonitor() {
		final Connector connector = new Connector();
		final Discovery discovery = Discovery.builder().build();
		final HardwareMonitor expected = HardwareMonitor.builder().type(MonitorType.ENCLOSURE).discovery(discovery)
				.build();
		assertEquals(expected, processor.createHardwareMonitor(ENCLOSURE, connector));
		assertEquals(1, connector.getHardwareMonitors().size());
		assertEquals(expected, connector.getHardwareMonitors().get(0));
	}

	@Test
	void testGetTextInstanceTable() {
		assertEquals(CONTAINS_DOUBLE_QUOTES_BUT_NOT_START_END, processor.getTextInstanceTable(CONTAINS_DOUBLE_QUOTES_BUT_NOT_START_END).getText());
		assertEquals(NO_DOUBLE_QUOTES, processor.getTextInstanceTable(NO_DOUBLE_QUOTES).getText());
		assertEquals(START_WITH_DOUBLE_QUOTES_ONLY, processor.getTextInstanceTable(START_WITH_DOUBLE_QUOTES_ONLY).getText());
		assertEquals(END_WITH_DOUBLE_QUOTES_ONLY, processor.getTextInstanceTable(END_WITH_DOUBLE_QUOTES_ONLY).getText());
		assertEquals(EMPTY, processor.getTextInstanceTable(EMPTY).getText());
	}
}
