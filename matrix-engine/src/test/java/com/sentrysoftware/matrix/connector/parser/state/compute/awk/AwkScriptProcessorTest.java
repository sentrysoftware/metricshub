package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;

public class AwkScriptProcessorTest {

	private AwkScriptProcessor awkScriptProcessor = new AwkScriptProcessor();

	private final Connector connector = new Connector();

	private static final String AWK_SCRIPT_KEY = "enclosure.discovery.source(1).compute(1).AwkScript";
	private static final String VALUE = "EmbeddedFile(1)";

	@Test
	void testParse() {

		{
			final Awk awk = Awk
					.builder()
					.index(1)
					.build();

			final SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
					.builder()
					.index(1)
					.computes(Collections.singletonList(awk))
					.build();

			final Discovery discovery = Discovery
					.builder()
					.sources(Collections.singletonList(snmpGetTableSource))
					.build();

			final HardwareMonitor hardwareMonitor = HardwareMonitor
					.builder()
					.type(MonitorType.ENCLOSURE)
					.discovery(discovery)
					.build();

			connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

			assertThrows(IllegalStateException.class, () -> awkScriptProcessor.parse(AWK_SCRIPT_KEY, VALUE, connector));

			final EmbeddedFile embeddedFile = EmbeddedFile
					.builder()
					.content("Embedded File content.")
					.type("Embedded")
					.build();

			connector.setEmbeddedFiles(Collections.singletonMap(1, embeddedFile));

			awkScriptProcessor.parse(AWK_SCRIPT_KEY, VALUE, connector);

			assertEquals(embeddedFile, ((Awk) snmpGetTableSource.getComputes().get(0)).getAwkScript());
		}
		
		// case embedded file directely in AwkScript
		{
			final String key = "PhysicalDisk.Collect.Source(1).Compute(1).AwkScript";
			final String value = "/^PV Status/ {print \"\"MSHW;\"\" $3 \"\"-\"\" $4}";
			
			final Awk awk = Awk
					.builder()
					.index(1)
					.build();

			final SnmpGetTableSource snmpGetTableSource = SnmpGetTableSource
					.builder()
					.index(1)
					.computes(Collections.singletonList(awk))
					.build();

			final Collect collect = Collect
					.builder()
					.sources(Collections.singletonList(snmpGetTableSource))
					.build();

			final HardwareMonitor hardwareMonitor = HardwareMonitor
					.builder()
					.type(MonitorType.PHYSICAL_DISK)
					.collect(collect)
					.build();

			connector
			.getHardwareMonitors()
			.add(hardwareMonitor);

			awkScriptProcessor.parse(key, value, connector);

			assertEquals(
					EmbeddedFile.builder().content(value).build(), 
					((Awk) snmpGetTableSource.getComputes().get(0)).getAwkScript());
		}
	}
}
