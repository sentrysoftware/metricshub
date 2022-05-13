package com.sentrysoftware.hardware.agent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HardwareSentryAgentAppTest {

	@Test
	void contextLoads() {
		assertDoesNotThrow(() -> {});
	}

	@Test
	void testInitializeLoggerContext() {
		assertDoesNotThrow(() -> HardwareSentryAgentApp.initializeLoggerContext());
	}
}
