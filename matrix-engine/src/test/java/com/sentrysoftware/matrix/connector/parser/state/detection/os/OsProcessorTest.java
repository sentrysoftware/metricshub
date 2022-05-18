package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;

class OsProcessorTest {

	@Test
	void testGetType() {
		assertEquals(Os.class, new KeepOnlyProcessor().getType());
		assertEquals(Os.class, new ExcludeProcessor().getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals(OsProcessor.OS_TYPE_VALUE, new KeepOnlyProcessor().getTypeValue());
		assertEquals(OsProcessor.OS_TYPE_VALUE, new ExcludeProcessor().getTypeValue());
	}
}
