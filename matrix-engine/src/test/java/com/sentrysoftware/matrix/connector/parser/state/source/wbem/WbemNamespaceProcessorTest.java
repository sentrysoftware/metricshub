package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;

class WbemNamespaceProcessorTest {

	private static final String WBEM_NAMESPACE_KEY = "enclosure.discovery.source(1).WbemNameSpace";
	private static final String NAMESPACE_VALUE = "root/emc/smis";

	private WbemNamespaceProcessor wbemNamespaceProcessor = new WbemNamespaceProcessor(WbemSource.class, "WBEM");

	private Connector connector;

	@BeforeEach
	void setUp() {
		connector = new Connector();
	}

	@Test
	void testParse() {

		final WbemSource wbemQuery = WbemSource.builder()
				.index(1)
				.build();

		final Discovery discovery = Discovery.builder()
				.sources(Collections.singletonList(wbemQuery))
				.build();

		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		wbemNamespaceProcessor.parse(WBEM_NAMESPACE_KEY, NAMESPACE_VALUE, connector);

		assertEquals(NAMESPACE_VALUE, wbemQuery.getWbemNamespace());

	}
}
