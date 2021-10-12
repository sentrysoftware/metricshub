package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcludeMatchingLinesProcessorTest {

	@Test
	void testGetType() {

		assertEquals(ExcludeMatchingLines.class, new RegexpProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ExcludeMatchingLinesProcessor.EXCLUDE_MATCHING_LINES_TYPE_VALUE,
			new RegexpProcessor().getTypeValue());
	}
}