package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;

public class ExtractPropertyFromWbemPathProcessorTest {
	
	@Test
	void testGetType() {

		assertEquals(ExtractPropertyFromWbemPath.class, new PropertyNameProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ExtractPropertyFromWbemPathProcessor.EXTRACT_PROPERTY_FROM_WBEM_PATH_TYPE_VALUE, new PropertyNameProcessor().getTypeValue());
	}
}
