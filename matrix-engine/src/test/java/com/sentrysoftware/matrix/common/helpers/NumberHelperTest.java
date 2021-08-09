package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NumberHelperTest {
	@Test
	void testParseDouble() {
		assertEquals(5.0, NumberHelper.parseDouble("not a number", 5.0));
		assertEquals(7.0, NumberHelper.parseDouble("7", 5.0));
		assertEquals(7.0, NumberHelper.parseDouble("7.0", 5.0));
	}

	@Test
	void testParseInt() {
		assertEquals(5, NumberHelper.parseInt("not a number", 5));
		assertEquals(7, NumberHelper.parseInt("7", 5));
		assertEquals(5, NumberHelper.parseInt("7.0", 5));
	}

	@Test
	void testRound() {

		assertEquals(20D, NumberHelper.round(20.000001, 2));
		assertEquals(20, NumberHelper.round(20.000001, 0));
		assertEquals(20D, NumberHelper.round(20.00, 1));
		assertEquals(20.1112, NumberHelper.round(20.11125, 4));
		assertEquals(5, NumberHelper.round(4.5, 0));
		assertEquals(4.5, NumberHelper.round(4.5, 1));
	}
}
