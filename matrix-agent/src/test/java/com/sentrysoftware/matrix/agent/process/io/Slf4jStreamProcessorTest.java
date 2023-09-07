package com.sentrysoftware.matrix.agent.process.io;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.sentrysoftware.matrix.agent.process.config.Slf4jLevel;

class Slf4jStreamProcessorTest {

	@Test
	void test() {
		assertDoesNotThrow(() -> ProcessorHelper.logger(LoggerFactory.getLogger(Slf4jStreamProcessorTest.class), Slf4jLevel.DEBUG).process(null));
	}

}
