package com.sentrysoftware.hardware.prometheus;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextException;

@SpringBootTest
class HardwareSentryPrometheusAppTest {

	@Test
	void contextLoads() {
		assertDoesNotThrow(() -> {}); 
	}

	@Test
	void testMain() {

		assertDoesNotThrow(() -> HardwareSentryPrometheusApp.main(new String[]{}));
	}
}
