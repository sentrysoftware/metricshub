package com.sentrysoftware.matrix.connector.parser.state.compute.substract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubstractProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Substract.class, new SubstractPropertyProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(SubstractProcessor.SUBSTRACT_TYPE_VALUE, new SubstractPropertyProcessor().getTypeValue());
	}
}