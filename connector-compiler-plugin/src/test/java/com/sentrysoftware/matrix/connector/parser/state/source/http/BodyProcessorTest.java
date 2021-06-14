package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.body.EmbeddedFileBody;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

public class BodyProcessorTest {

	private static final String BODY_KEY = "enclosure.discovery.source(3).body";
	private static final String VALUE_STRING = "<?xml version='1.0' encoding='utf-8' ?>";
	private static final String VALUE_EMBEDDED_FILE = "EmbeddedFile(1)";

	private final Connector connector = new Connector();
	private final BodyProcessor bodyProcessor = new BodyProcessor();

	@Test
	void testParseStringBody() {

		HTTPSource httpSource = HTTPSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		bodyProcessor.parse(BODY_KEY, VALUE_STRING, connector);
		Body body = new StringBody(VALUE_STRING);
		assertEquals(body, httpSource.getBody());
	}

	@Test
	void testParseEmbeddedFileBody() {

		HTTPSource httpSource = HTTPSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		EmbeddedFile embeddedFile = EmbeddedFile
				.builder()
				.content("Embedded File content.")
				.type("Embedded")
				.build();

		connector.setEmbeddedFiles(Collections.singletonMap(1, embeddedFile));

		bodyProcessor.parse(BODY_KEY, VALUE_EMBEDDED_FILE, connector);
		Body body = new EmbeddedFileBody(embeddedFile);
		assertEquals(body, httpSource.getBody());
	}
}
