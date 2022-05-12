package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class ErrorStatusTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), ErrorStatus.interpret(null));
		assertEquals(Optional.empty(), ErrorStatus.interpret(""));
		assertEquals(Optional.empty(), ErrorStatus.interpret("unknown"));
	}

	@Test
	void testInterpretOK() {
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret("0"));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret("0.0"));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret("ok"));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret(" ok "));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret(" Ok "));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret(" OK "));
		assertEquals(Optional.of(ErrorStatus.NONE), ErrorStatus.interpret("OK"));
	}

	@Test
	void testInterpretDetected() {
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("1"));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("1.0"));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("warn"));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret(" warn "));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret(" WARN "));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("WARN"));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("warning"));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret(" warning "));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret(" WARNING "));
		assertEquals(Optional.of(ErrorStatus.DETECTED), ErrorStatus.interpret("WARNING"));
	}

	@Test
	void testInterpretTooMany() {
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret("2.0"));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret("2"));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret("2 "));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret("alarm"));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret(" alarm "));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret(" Alarm "));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret(" ALARM "));
		assertEquals(Optional.of(ErrorStatus.TOO_MANY), ErrorStatus.interpret("ALARM"));
	}

}