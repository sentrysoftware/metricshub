package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtractProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Extract.class, new SubColumnProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ExtractProcessor.EXTRACT_TYPE_VALUE, new SubColumnProcessor().getTypeValue());
	}
}