package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TableJoinProcessorTest {

	@Test
	void testGetType() {

		assertEquals(TableJoinProcessor.TABLE_JOIN_TYPE_VALUE, new LeftTableProcessor().getTypeValue());
	}
}