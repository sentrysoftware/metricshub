package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NumberHelperTest {
	@Test
	void parseDoubleTest() {
		assertEquals(5.0, NumberHelper.parseDouble("not a number", 5.0));
		assertEquals(7.0, NumberHelper.parseDouble("7", 5.0));
		assertEquals(7.0, NumberHelper.parseDouble("7.0", 5.0));
	}

	@Test
	void parseIntTest() {
		assertEquals(5, NumberHelper.parseInt("not a number", 5));
		assertEquals(7, NumberHelper.parseInt("7", 5));
		assertEquals(5, NumberHelper.parseInt("7.0", 5));
	}
}
