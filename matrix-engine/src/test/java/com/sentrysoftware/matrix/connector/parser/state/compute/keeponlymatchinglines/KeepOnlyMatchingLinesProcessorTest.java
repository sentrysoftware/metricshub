package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class KeepOnlyMatchingLinesProcessorTest {

	@Test
	void testGetType() {

		assertEquals(KeepOnlyMatchingLines.class, new RegexpProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(KeepOnlyMatchingLinesProcessor.KEEP_ONLY_MATCHING_LINES_TYPE_VALUE,
			new RegexpProcessor().getTypeValue());
	}
}