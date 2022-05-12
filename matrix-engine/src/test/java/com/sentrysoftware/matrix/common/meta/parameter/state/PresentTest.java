package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class PresentTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), Present.interpret(null));
		assertEquals(Optional.empty(), Present.interpret(""));
		assertEquals(Optional.empty(), Present.interpret("unknown"));
	}

	@Test
	void testInterpretMissing() {
		assertEquals(Optional.of(Present.MISSING), Present.interpret("0"));
		assertEquals(Optional.of(Present.MISSING), Present.interpret("0.0"));
		assertEquals(Optional.of(Present.MISSING), Present.interpret(" 0.0 "));
		assertEquals(Optional.of(Present.MISSING), Present.interpret("0.0 "));
	}

	@Test
	void testInterpretPresent() {
		assertEquals(Optional.of(Present.PRESENT), Present.interpret("1"));
		assertEquals(Optional.of(Present.PRESENT), Present.interpret("1.0"));
		assertEquals(Optional.of(Present.PRESENT), Present.interpret("1.0  "));
		assertEquals(Optional.of(Present.PRESENT), Present.interpret("  1.0"));
	}

}
