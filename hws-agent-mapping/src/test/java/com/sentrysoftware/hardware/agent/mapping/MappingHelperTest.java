package com.sentrysoftware.hardware.agent.mapping;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MappingHelperTest {

	@Test
	void testCamelCaseToSnakeCase() {
		assertEquals("system_size", MappingHelper.camelCaseToSnakeCase("systemSize"));
		assertEquals("system_size", MappingHelper.camelCaseToSnakeCase("system_size"));
	}

	@Test
	void testSnakeCaseToCamelCase() {
		assertEquals("systemSize", MappingHelper.snakeCaseToCamelCase("system_size"));
		assertEquals("systemSize", MappingHelper.snakeCaseToCamelCase("systemSize"));
	}

}
