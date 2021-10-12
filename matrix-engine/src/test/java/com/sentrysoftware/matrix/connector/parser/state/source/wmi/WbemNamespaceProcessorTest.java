package com.sentrysoftware.matrix.connector.parser.state.source.wmi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.collect.Collect;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;

class WbemNamespaceProcessorTest {

	private static final String WBEM_NAMESPACE = "root/emc";
	private static final String WBEM_NAMESPACE_KEY = "enclosure.collect.source(1).WbemNameSpace";

	private static WbemNamespaceProcessor wbemNamespaceProcessor = new WbemNamespaceProcessor(WMISource.class, "WMI");

	private Connector connector;

	@BeforeEach
	void setUp() {
		connector = new Connector();
	}

	@Test
	void testParse() {

		final Collect collect = Collect.builder()
				.sources(Collections.singletonList(WMISource.builder()
						.key("enclosure.collect.source(1)")
						.index(1).build()))
				.build();

		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder()
				.type(MonitorType.ENCLOSURE)
				.collect(collect)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		wbemNamespaceProcessor.parse(WBEM_NAMESPACE_KEY, WBEM_NAMESPACE, connector);

		final WMISource expected = WMISource.builder()
				.index(1)
				.wbemNamespace(WBEM_NAMESPACE)
				.key("enclosure.collect.source(1)")
				.build();

		assertEquals(expected, collect.getSources().get(0));
}

}
