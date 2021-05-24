package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiplyProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Multiply.class, new MultiplyByProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(MultiplyProcessor.MULTIPLY_TYPE_VALUE, new MultiplyByProcessor().getTypeValue());
	}
}