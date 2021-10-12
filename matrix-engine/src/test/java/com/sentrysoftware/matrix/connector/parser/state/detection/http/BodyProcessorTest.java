package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.body.EmbeddedFileBody;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BodyProcessorTest {

	private final BodyProcessor bodyProcessor = new BodyProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "detection.criteria(1).body";
	private static final String FOO = "FOO";
	private static final String EXISTING_EMBEDDED_FILE = "EmbeddedFile(1)";
	private static final String UNKNOWN_EMBEDDED_FILE = "EmbeddedFile(2)";

	@Test
	void testParse() {

		// String header
		HTTP http = HTTP.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(http)).build();
		connector.setDetection(detection);
		assertNull(http.getBody());
		bodyProcessor.parse(KEY, FOO, connector);
		assertEquals(new StringBody(FOO), http.getBody());

		// Existing embedded file
		EmbeddedFile embeddedFile = new EmbeddedFile();
		connector.getEmbeddedFiles().put(1, embeddedFile);
		bodyProcessor.parse(KEY, EXISTING_EMBEDDED_FILE, connector);
		assertEquals(new EmbeddedFileBody(embeddedFile), http.getBody());

		// Unknown embedded file
		bodyProcessor.parse(KEY, UNKNOWN_EMBEDDED_FILE, connector);
		assertEquals(new EmbeddedFileBody(null), http.getBody());
	}
}