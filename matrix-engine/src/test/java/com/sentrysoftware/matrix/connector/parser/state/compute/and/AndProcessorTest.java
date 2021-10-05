package com.sentrysoftware.matrix.connector.parser.state.compute.and;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;

public class AndProcessorTest {
	@Test
	void testGetType() {

		assertEquals(And.class, new AndPropertyProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(AndProcessor.AND_TYPE_VALUE, new AndPropertyProcessor().getTypeValue());
	}
}
