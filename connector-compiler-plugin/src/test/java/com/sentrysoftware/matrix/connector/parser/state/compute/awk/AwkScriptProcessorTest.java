package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;

public class AwkScriptProcessorTest {

	private AwkScriptProcessor awkScriptProcessor = new AwkScriptProcessor();

	private final Connector connector = new Connector();

	private static final String AWK_SCRIPT_KEY = "enclosure.discovery.source(1).compute(1).AwkScript";
	private static final String VALUE = "EmbeddedFile(1)";

	@Test
	void testParse() {

		Awk awk = Awk
				.builder()
				.index(1)
				.build();

		SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource
				.builder()
				.index(1)
				.computes(Collections.singletonList(awk))
				.build();

		Discovery discovery = Discovery
				.builder()
				.sources(Collections.singletonList(snmpGetTableSource))
				.build();

		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector
		.getHardwareMonitors()
		.add(hardwareMonitor);

		assertThrows(IllegalStateException.class, () -> awkScriptProcessor.parse(AWK_SCRIPT_KEY, VALUE, connector));

		EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("Embedded File content.")
				.type("Embedded")
				.build();

		connector.setEmbeddedFiles(Collections.singletonMap(1, embeddedFile));

		awkScriptProcessor.parse(AWK_SCRIPT_KEY, VALUE, connector);

		assertEquals(embeddedFile, ((Awk) snmpGetTableSource.getComputes().get(0)).getAwkScript());
	}
}
