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
	void testInterpretStandby() {
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret("1"));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret("1.0"));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret("standby"));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret(" standby "));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret(" Standby "));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret(" STANDBY "));
		assertEquals(Optional.of(PowerState.STANDBY), PowerState.interpret("STANDBY"));
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
