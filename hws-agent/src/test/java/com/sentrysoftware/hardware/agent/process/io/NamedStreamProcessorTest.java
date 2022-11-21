package com.sentrysoftware.hardware.agent.process.io;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class NamedStreamProcessorTest {

	private static final String TAG = "tag";

	@Test
	void test() { // NOSONAR This method already performs assertions
		NamedStreamProcessor processor = new NamedStreamProcessor(TAG, new TestStreamProcessor());
		processor.process("value");
	}

	class TestStreamProcessor implements StreamProcessor {

		@Override
		public void process(String block) {
			assertTrue(block.startsWith(TAG));
		}

	}
}
