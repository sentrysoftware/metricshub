package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class StatusTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), Status.interpret(null));
		assertEquals(Optional.empty(), Status.interpret(""));
		assertEquals(Optional.empty(), Status.interpret("unknown"));
	}

	@Test
	void testInterpretOK() {
		assertEquals(Optional.of(Status.OK), Status.interpret("0"));
		assertEquals(Optional.of(Status.OK), Status.interpret("0.0"));
		assertEquals(Optional.of(Status.OK), Status.interpret("ok"));
		assertEquals(Optional.of(Status.OK), Status.interpret(" ok "));
		assertEquals(Optional.of(Status.OK), Status.interpret(" Ok "));
		assertEquals(Optional.of(Status.OK), Status.interpret(" OK "));
		assertEquals(Optional.of(Status.OK), Status.interpret("OK"));
	}

	@Test
	void testInterpretDegraded() {
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("1"));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("1.0"));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("warn"));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret(" warn "));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret(" WARN "));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("WARN"));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("warning"));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret(" warning "));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret(" WARNING "));
		assertEquals(Optional.of(Status.DEGRADED), Status.interpret("WARNING"));
	}

	@Test
	void testInterpretFailed() {
		assertEquals(Optional.of(Status.FAILED), Status.interpret("2.0"));
		assertEquals(Optional.of(Status.FAILED), Status.interpret("2"));
		assertEquals(Optional.of(Status.FAILED), Status.interpret("2 "));
		assertEquals(Optional.of(Status.FAILED), Status.interpret("alarm"));
		assertEquals(Optional.of(Status.FAILED), Status.interpret(" alarm "));
		assertEquals(Optional.of(Status.FAILED), Status.interpret(" Alarm "));
		assertEquals(Optional.of(Status.FAILED), Status.interpret(" ALARM "));
		assertEquals(Optional.of(Status.FAILED), Status.interpret("ALARM"));
	}

}
