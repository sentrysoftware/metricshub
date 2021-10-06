package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class IntrusionStatusTest {

	@Test
	void testInterpretEmpty() {
		assertEquals(Optional.empty(), IntrusionStatus.interpret(null));
		assertEquals(Optional.empty(), IntrusionStatus.interpret(""));
		assertEquals(Optional.empty(), IntrusionStatus.interpret("unknown"));
	}

	@Test
	void testInterpretClosedFromOk() {
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret("0"));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret("0.0"));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret("ok"));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret(" ok "));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret(" Ok "));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret(" OK "));
		assertEquals(Optional.of(IntrusionStatus.CLOSED), IntrusionStatus.interpret("OK"));
	}

	@Test
	void testInterpretOpenFromWarn() {
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("1"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("1.0"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("warn"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" warn "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" WARN "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("WARN"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("warning"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" warning "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" WARNING "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("WARNING"));
	}

	@Test
	void testInterpretOpenFromAlarm() {
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("2.0"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("2"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("2 "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("alarm"));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" alarm "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" Alarm "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret(" ALARM "));
		assertEquals(Optional.of(IntrusionStatus.OPEN), IntrusionStatus.interpret("ALARM"));
	}
}
