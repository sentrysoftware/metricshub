package com.sentrysoftware.matrix.connector.parser.state.detection.process;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;

public class ProcessCommandLineProcessorTest {
	private final ProcessCommandLineProcessor processCommandLineProcessor = new ProcessCommandLineProcessor();

	private final Connector connector = new Connector();

	private static final String KEY = "detection.criteria(1).processcommandline";
	private static final String FOO = "FOO";

	@Test
	void testParse() {

		Process process = Process.builder().index(1).build();
		Detection detection = Detection.builder().criteria(Collections.singletonList(process)).build();
		connector.setDetection(detection);
		assertNull(process.getProcessCommandLine());
		processCommandLineProcessor.parse(KEY, FOO, connector);
		assertEquals(FOO, process.getProcessCommandLine());
	}
}
