package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class PowerStateTest {

	@Test
	void testInterpretEmpty() {

		assertEquals(Optional.empty(), PowerState.interpret(null));
		assertEquals(Optional.empty(), PowerState.interpret(""));
		assertEquals(Optional.empty(), PowerState.interpret("unknown"));
	}

	@Test
	void testInterpretOff() {
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret("0"));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret("0.0"));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret("off"));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret(" off "));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret(" Off "));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret(" OFF "));
		assertEquals(Optional.of(PowerState.OFF), PowerState.interpret("OFF"));
	}

	@Test
	void testInterpretSuspended() {
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret("1"));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret("1.0"));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret("suspended"));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret(" suspended "));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret(" Suspended "));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret(" SUSPENDED "));
		assertEquals(Optional.of(PowerState.SUSPENDED), PowerState.interpret("SUSPENDED"));
	}

	@Test
	void testInterpretOn() {
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret("2.0"));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret("2"));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret("2 "));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret("on"));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret(" on "));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret(" On "));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret(" ON "));
		assertEquals(Optional.of(PowerState.ON), PowerState.interpret("ON"));
	}

}
