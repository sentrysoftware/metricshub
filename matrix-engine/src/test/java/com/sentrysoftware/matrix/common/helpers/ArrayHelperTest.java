package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ArrayHelperTest {

	@Test
	void getValueAtIndexTest() {
		Integer[] array = {1, 2, 3, 4};
		assertEquals(3, ArrayHelper.getValueAtIndex(array, 2, 6));
		assertEquals(6, ArrayHelper.getValueAtIndex(array, 5, 6));
	}
}
