package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

public class UrlProcessorTest {
	private static final String URL_KEY = "enclosure.discovery.source(3).url";
	private static final String VALUE = "/api/rest/StorageCenter/ScChassis/PowerSupplyList";

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

		new UrlProcessor().parse(URL_KEY, VALUE, connector);
		assertEquals(VALUE, httpSource.getUrl());
	}
}
