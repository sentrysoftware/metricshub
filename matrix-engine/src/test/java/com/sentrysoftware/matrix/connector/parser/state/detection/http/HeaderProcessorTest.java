package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.header.EmbeddedFileHeader;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.Http;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HeaderProcessorTest {

	private final HeaderProcessor headerProcessor = new HeaderProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "detection.criteria(1).header";
	private static final String FOO = "FOO";
	private static final String EXISTING_EMBEDDED_FILE = "EmbeddedFile(1)";
	private static final String UNKNOWN_EMBEDDED_FILE = "EmbeddedFile(2)";

	@Test
	void testParse() {

		// String header
		Http http = Http.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(http)).build();
		connector.setDetection(detection);
		assertNull(http.getHeader());
		headerProcessor.parse(KEY, FOO, connector);
		assertEquals(new StringHeader(FOO), http.getHeader());

		// Existing embedded file
		EmbeddedFile embeddedFile = new EmbeddedFile();
		connector.getEmbeddedFiles().put(1, embeddedFile);
		headerProcessor.parse(KEY, EXISTING_EMBEDDED_FILE, connector);
		assertEquals(new EmbeddedFileHeader(embeddedFile), http.getHeader());

		// Unknown embedded file
		headerProcessor.parse(KEY, UNKNOWN_EMBEDDED_FILE, connector);
		assertEquals(new EmbeddedFileHeader(null), http.getHeader());
	}
}