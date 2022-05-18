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
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

class WbemQueryProcessorTest {

	private static final String WBEM_QUERY_KEY = "enclosure.discovery.source(1).WbemQuery";
	private static final String QUERY_VALUE = "SELECT __PATH,Model,SerialNumber FROM EMC_VNXe_ArrayChassisLeaf";

	private WbemQueryProcessor wbemQueryProcessor = new WbemQueryProcessor(WbemSource.class, "WBEM");

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

		final HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		wbemQueryProcessor.parse(WBEM_QUERY_KEY, QUERY_VALUE, connector);

		assertEquals(QUERY_VALUE, wbemQuery.getWbemQuery());

	}
}
