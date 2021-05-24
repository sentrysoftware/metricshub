package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplaceProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Replace.class, new ReplaceByProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ReplaceProcessor.REPLACE_TYPE_VALUE, new ReplaceByProcessor().getTypeValue());
	}
}