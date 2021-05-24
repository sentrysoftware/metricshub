package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AddProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Add.class, new AddPropertyProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(AddProcessor.ADD_TYPE_VALUE, new AddPropertyProcessor().getTypeValue());
	}
}