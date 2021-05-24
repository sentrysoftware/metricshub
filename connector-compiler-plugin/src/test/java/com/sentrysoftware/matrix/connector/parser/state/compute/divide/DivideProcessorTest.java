package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DivideProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Divide.class, new DivideByProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(DivideProcessor.DIVIDE_TYPE_VALUE, new DivideByProcessor().getTypeValue());
	}
}