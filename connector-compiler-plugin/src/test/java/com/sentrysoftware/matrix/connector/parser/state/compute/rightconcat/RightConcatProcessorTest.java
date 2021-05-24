package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RightConcatProcessorTest {

	@Test
	void testGetType() {

		assertEquals(RightConcat.class, new StringProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(RightConcatProcessor.RIGHT_CONCAT_TYPE_VALUE, new StringProcessor().getTypeValue());
	}
}