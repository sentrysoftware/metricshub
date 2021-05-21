package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeftConcatProcessorTest {

	@Test
	void testGetType() {

		assertEquals(LeftConcat.class, new StringProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(LeftConcatProcessor.LEFT_CONCAT_TYPE_VALUE, new StringProcessor().getTypeValue());
	}
}