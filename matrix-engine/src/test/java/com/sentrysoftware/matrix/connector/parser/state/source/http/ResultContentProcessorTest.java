package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HttpSource;

class ResultContentProcessorTest {
	private static final String RESULT_CONTENT_KEY = "enclosure.discovery.source(3).resultcontent";


	private static final String WRONG_VALUE = "boddy";

	private static final String VALUE = "body";
	private static final ResultContent RESULT = ResultContent.BODY;

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

		assertThrows(IllegalStateException.class, () -> new ResultContentProcessor().parse(RESULT_CONTENT_KEY, WRONG_VALUE, connector));

		new ResultContentProcessor().parse(RESULT_CONTENT_KEY, VALUE, connector);
		assertEquals(RESULT, httpSource.getResultContent());
	}
}
