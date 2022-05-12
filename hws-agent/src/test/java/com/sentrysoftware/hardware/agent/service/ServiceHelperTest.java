package com.sentrysoftware.hardware.agent.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ServiceHelperTest {

	@Test
	void testCamelCaseToSnakeCase() {
		assertEquals("system_size", ServiceHelper.camelCaseToSnakeCase("systemSize"));
		assertEquals("system_size", ServiceHelper.camelCaseToSnakeCase("system_size"));
	}

	@Test
	void testSnakeCaseToCamelCase() {
		assertEquals("systemSize", ServiceHelper.snakeCaseToCamelCase("system_size"));
		assertEquals("systemSize", ServiceHelper.snakeCaseToCamelCase("systemSize"));
	}

}
