package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class LedIndicatorTest {

	@Test
	void testInterpretEmpty() {
		assertEquals(Optional.empty(), LedIndicator.interpret(null));
		assertEquals(Optional.empty(), LedIndicator.interpret(""));
		assertEquals(Optional.empty(), LedIndicator.interpret("unknown"));
	}

	@Test
	void testInterpretBlinking() {
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret("1"));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret("1.0"));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret("blinking"));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret(" blinking "));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret(" Blinking "));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret(" BLINKING "));
		assertEquals(Optional.of(LedIndicator.BLINKING), LedIndicator.interpret("BLINKING"));
	}

	@Test
	void testInterpretOff() {
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret("0"));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret("0.0"));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret("off"));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret(" off "));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret(" Off "));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret(" OFF "));
		assertEquals(Optional.of(LedIndicator.OFF), LedIndicator.interpret("OFF"));
	}

	@Test
	void testInterpretOn() {
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret("2.0"));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret("2"));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret("2 "));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret("on"));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret(" on "));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret(" On "));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret(" ON "));
		assertEquals(Optional.of(LedIndicator.ON), LedIndicator.interpret("ON"));
	}

}
