package com.sentrysoftware.matrix.connector.parser.state.source.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.header.EmbeddedFileHeader;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.connector.model.monitor.job.discovery.Discovery;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;

class HeaderProcessorTest {

	private static final String HEADER_KEY = "enclosure.discovery.source(3).header";
	private static final String VALUE_STRING = "<?xml version='1.0' encoding='utf-8' ?>";
	private static final String VALUE_EMBEDDED_FILE = "EmbeddedFile(1)";

	private final Connector connector = new Connector();
	private final HeaderProcessor headerProcessor = new HeaderProcessor();

	@Test
	void testParseStringHeader() {

		HTTPSource httpSource = HTTPSource.builder().index(3).build();
		Discovery discovery = Discovery.builder().sources(Collections.singletonList(httpSource)).build();
		HardwareMonitor hardwareMonitor = HardwareMonitor
				.builder()
				.type(MonitorType.ENCLOSURE)
				.discovery(discovery)
				.build();

		connector.setHardwareMonitors(Collections.singletonList(hardwareMonitor));

		headerProcessor.parse(HEADER_KEY, VALUE_STRING, connector);
		Header header = new StringHeader(VALUE_STRING);
		assertEquals(header, httpSource.getHeader());
	}

	@Test
	void testParseEmbeddedFileHeader() {

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

		headerProcessor.parse(HEADER_KEY, VALUE_EMBEDDED_FILE, connector);
		Header header = new EmbeddedFileHeader(embeddedFile);
		assertEquals(header, httpSource.getHeader());
	}
}
