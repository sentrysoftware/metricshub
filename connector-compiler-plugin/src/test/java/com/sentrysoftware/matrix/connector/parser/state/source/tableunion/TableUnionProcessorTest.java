package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableUnionProcessorTest {

	@Test
	void testGetTypeValue() {
		assertEquals(TableUnionProcessor.TABLE_UNION_TYPE_VALUE, new TableProcessor().getTypeValue());
	}
}