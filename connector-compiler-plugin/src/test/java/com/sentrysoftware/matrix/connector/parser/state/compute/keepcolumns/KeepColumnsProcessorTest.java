package com.sentrysoftware.matrix.connector.parser.state.compute.keepcolumns;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeepColumnsProcessorTest {

	@Test
	void testGetType() {

		assertEquals(KeepColumns.class, new ColumnNumbersProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(KeepColumnsProcessor.KEEP_COLUMNS_TYPE_VALUE, new ColumnNumbersProcessor().getTypeValue());
	}
}