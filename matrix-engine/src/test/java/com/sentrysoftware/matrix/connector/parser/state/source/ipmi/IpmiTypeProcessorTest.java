package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;

class IpmiTypeProcessorTest {

	private static final String KEY = "Enclosure.Discovery.Source(1).Type";
	private static final String SOURCE_KEY = "Enclosure.Discovery.Source(1)";
	private static final String VALUE = "IPMI";
	private static final List<String> SUDO_COMMAND = Collections.singletonList("ipmitool");

	@Test
	void testParse() {
		Connector connector = new Connector();

		new IpmiTypeProcessor(Ipmi.class, IpmiTypeProcessor.IPMI_TYPE_VALUE).parse(KEY, VALUE, connector);

		assertEquals(SUDO_COMMAND, connector.getSudoCommands());
		assertNotNull(connector.getHardwareMonitors());
		assertEquals(1, connector.getHardwareMonitors().size());

		HardwareMonitor hardwareMonitor = connector.getHardwareMonitors().get(0);
		assertNotNull(hardwareMonitor);

		Discovery discovery = hardwareMonitor.getDiscovery();
		assertNotNull(discovery);
		assertNotNull(discovery.getSources());
		assertEquals(1, discovery.getSources().size());

		Source source = discovery.getSources().get(0);
		assertNotNull(source);
		assertTrue(source instanceof Ipmi);
		assertEquals(1, source.getIndex());
		assertEquals(SOURCE_KEY, source.getKey());
	}
}
