package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;

public class OsProcessorTest {

	@Test
	void testGetType() {
		assertEquals(OS.class, new KeepOnlyProcessor().getType());
		assertEquals(OS.class, new ExcludeProcessor().getType());
	}

	@Test
	void testGetTypeValue() {
		assertEquals(OsProcessor.OS_TYPE_VALUE, new KeepOnlyProcessor().getTypeValue());
		assertEquals(OsProcessor.OS_TYPE_VALUE, new ExcludeProcessor().getTypeValue());
	}
}
