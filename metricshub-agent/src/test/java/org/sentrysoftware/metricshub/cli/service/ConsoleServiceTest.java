package org.sentrysoftware.metricshub.cli.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ConsoleServiceTest {

	@Test
	void testHasConsole() {
		Assertions.assertDoesNotThrow(() -> ConsoleService.hasConsole());
	}
}
