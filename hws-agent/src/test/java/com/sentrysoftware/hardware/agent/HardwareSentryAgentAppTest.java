package com.sentrysoftware.hardware.agent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HardwareSentryAgentAppTest {

	@Test
	void contextLoads() {
		assertDoesNotThrow(() -> {});
	}

	@Test
	void testConfigureGlobalLogger() {
		assertDoesNotThrow(() -> 
			HardwareSentryAgentApp.configureGlobalLogger(
				new DefaultApplicationArguments(new String[] { Paths.get("--config=src/test/resources/data/hws-config.yaml").toString() })
			)
		);
	}

	@Test
	void testConfigureGlobalErrorLogger() {
		assertDoesNotThrow(() -> HardwareSentryAgentApp.configureGlobalErrorLogger());
	}

	@Test
	void testMain() {
		assertDoesNotThrow(() -> HardwareSentryAgentApp.main(new String[] {"--config=src/test/resources/data/hws-config.yaml"}));
	}
}
