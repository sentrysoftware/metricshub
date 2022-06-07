package com.sentrysoftware.matrix.connector.parser.state.source.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;
import com.sentrysoftware.matrix.connector.parser.state.detection.http.HttpProcessor;

class EntryConcatEndProcessorTest {

	private static final String ENTRY_CONCAT_END_KEY = "enclosure.discovery.source(3).entryconcatend";
	private static final String VALUE = "entry concat end";

	@Test
	void testParse() {

		HttpSource httpSource = HttpSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		new EntryConcatEndProcessor(HttpSource.class, HttpProcessor.HTTP_TYPE_VALUE).parse(ENTRY_CONCAT_END_KEY, VALUE, connector);
		assertEquals(VALUE, httpSource.getEntryConcatEnd());
	}
}