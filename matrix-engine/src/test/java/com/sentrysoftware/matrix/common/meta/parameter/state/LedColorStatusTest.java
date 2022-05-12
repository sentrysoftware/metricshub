package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class LedColorLedColorStatusTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), LedColorStatus.interpret(null));
		assertEquals(Optional.empty(), LedColorStatus.interpret(""));
		assertEquals(Optional.empty(), LedColorStatus.interpret("unknown"));
	}

	@Test
	void testInterpretOK() {
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret("0"));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret("0.0"));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret("ok"));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret(" ok "));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret(" Ok "));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret(" OK "));
		assertEquals(Optional.of(LedColorStatus.OK), LedColorStatus.interpret("OK"));
	}

	@Test
	void testInterpretDegraded() {
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("1"));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("1.0"));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("warn"));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret(" warn "));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret(" WARN "));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("WARN"));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("warning"));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret(" warning "));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret(" WARNING "));
		assertEquals(Optional.of(LedColorStatus.DEGRADED), LedColorStatus.interpret("WARNING"));
	}

	@Test
	void testInterpretFailed() {
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret("2.0"));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret("2"));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret("2 "));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret("alarm"));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret(" alarm "));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret(" Alarm "));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret(" ALARM "));
		assertEquals(Optional.of(LedColorStatus.FAILED), LedColorStatus.interpret("ALARM"));
	}

}