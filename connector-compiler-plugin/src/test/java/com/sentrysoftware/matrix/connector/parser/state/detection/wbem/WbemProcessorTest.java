package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WbemProcessorTest {

	@Test
	void testGetType() {

		assertEquals(WBEM.class, new WbemNameSpaceProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(WbemProcessor.WBEM_TYPE_VALUE, new WbemNameSpaceProcessor().getTypeValue());
	}
}