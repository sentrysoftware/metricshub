package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class NeedsCleaningTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), NeedsCleaning.interpret(null));
		assertEquals(Optional.empty(), NeedsCleaning.interpret(""));
		assertEquals(Optional.empty(), NeedsCleaning.interpret("unknown"));
	}

	@Test
	void testInterpretOK() {
		assertEquals(Optional.of(NeedsCleaning.OK), NeedsCleaning.interpret("0"));
		assertEquals(Optional.of(NeedsCleaning.OK), NeedsCleaning.interpret("0.0"));
		assertEquals(Optional.of(NeedsCleaning.OK), NeedsCleaning.interpret("0 "));
		assertEquals(Optional.of(NeedsCleaning.OK), NeedsCleaning.interpret("0.0 "));
	}

	@Test
	void testInterpretDegraded() {
		assertEquals(Optional.of(NeedsCleaning.NEEDED), NeedsCleaning.interpret("1"));
		assertEquals(Optional.of(NeedsCleaning.NEEDED), NeedsCleaning.interpret("1.0"));
		assertEquals(Optional.of(NeedsCleaning.NEEDED), NeedsCleaning.interpret(" 1.0 "));
	}

	@Test
	void testInterpretFailed() {
		assertEquals(Optional.of(NeedsCleaning.NEEDED_IMMEDIATELY), NeedsCleaning.interpret("2.0"));
		assertEquals(Optional.of(NeedsCleaning.NEEDED_IMMEDIATELY), NeedsCleaning.interpret(" 2 "));
		assertEquals(Optional.of(NeedsCleaning.NEEDED_IMMEDIATELY), NeedsCleaning.interpret("2"));
	}

}
