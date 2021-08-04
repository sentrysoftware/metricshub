package com.sentrysoftware.matrix.connector.parser.state.detection.process;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ProcessProcessorTest {
	@Test
	void testGetType() {

		assertEquals(com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process.class, new ProcessCommandLineProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ProcessProcessor.PROCESS_TYPE_VALUE, new ProcessCommandLineProcessor().getTypeValue());
	}
}
