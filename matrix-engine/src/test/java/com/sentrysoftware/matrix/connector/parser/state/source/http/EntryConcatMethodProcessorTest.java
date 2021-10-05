package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

class EntryConcatMethodProcessorTest {
	private static final String ENTRY_CONCAT_METHOD_KEY = "enclosure.discovery.source(3).entryconcatmethod";
	private static final String WRONG_VALUE = "listt";
	private static final String VALUE = "JSONArrayExtended";
	private static final EntryConcatMethod RESULT = EntryConcatMethod.JSON_ARRAY_EXTENDED;

	@Test
	void testParse() {

		HTTPSource httpSource = HTTPSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();
		Connector connector = new Connector();
		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		assertThrows(IllegalStateException.class, () -> new EntryConcatMethodProcessor().parse(ENTRY_CONCAT_METHOD_KEY, WRONG_VALUE, connector));

		new EntryConcatMethodProcessor().parse(ENTRY_CONCAT_METHOD_KEY, VALUE, connector);
		assertEquals(RESULT, httpSource.getEntryConcatMethod());
	}
}
