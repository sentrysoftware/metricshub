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
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

class WbemQueryProcessorTest {

	private static final String WBEM_QUERY = "SELECT Version FROM IBMPSG_DirectorAgent";
	private static final String WBEM_QUERY_KEY = "enclosure.collect.source(1).WbemQuery";

	private static WbemQueryProcessor wbemQueryProcessor = new WbemQueryProcessor(WMISource.class, "WMI");

	private Connector connector;

	@BeforeEach
	void setUp() {
		connector = new Connector();
	}

	@Test
	void testParse() {

		final Collect collect = Collect
				.builder()
				.sources(Collections.singletonList(WMISource.builder()
						.key("enclosure.collect.source(1)")
						.index(1).build()))
				.build();

		final HardwareMonitor hardwareMonitor = HardwareMonitor.builder()
				.type(MonitorType.ENCLOSURE)
				.collect(collect)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		wbemQueryProcessor.parse(WBEM_QUERY_KEY, WBEM_QUERY, connector);

		final WMISource expected = WMISource.builder()
				.index(1)
				.wbemQuery(WBEM_QUERY)
				.key("enclosure.collect.source(1)")
				.build();

		assertEquals(expected, collect.getSources().get(0));
	}


}
