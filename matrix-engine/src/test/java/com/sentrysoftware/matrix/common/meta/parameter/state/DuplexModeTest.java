package com.sentrysoftware.matrix.common.meta.parameter.state;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class DuplexModeTest {

	@Test
	void testInterpretNull() {
		assertEquals(Optional.empty(), DuplexMode.interpret(null));
		assertEquals(Optional.empty(), DuplexMode.interpret(""));
		assertEquals(Optional.empty(), DuplexMode.interpret("unknown"));
	}

	@Test
	void testInterpretHalf() {
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("0"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("0.0"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("no"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" no "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" No "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" NO "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("NO"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("half"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" half "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" Half "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" HALF "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("HALF"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("warn"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" warn "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" Warn "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" WARN "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("WARN"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("warning"));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" warning "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" Warning "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret(" WARNING "));
		assertEquals(Optional.of(DuplexMode.HALF), DuplexMode.interpret("WARNING"));
	}

	@Test
	void testInterpretFull() {
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("1"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("1.0"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("yes"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" yes "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" Yes "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" YES "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("YES"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("full"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" full "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" Full "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" FULL "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("FULL"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("ok"));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" ok "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" Ok "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret(" OK "));
		assertEquals(Optional.of(DuplexMode.FULL), DuplexMode.interpret("OK"));
	}

}
