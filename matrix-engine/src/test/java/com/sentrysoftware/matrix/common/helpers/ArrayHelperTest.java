package com.sentrysoftware.matrix.common.helpers;

import static org.junit.jupiter.api.Assertions.*;
import static com.sentrysoftware.matrix.common.helpers.ArrayHelper.*;
import org.junit.jupiter.api.Test;

class ArrayHelperTest {

	@Test
	void getValueAtIndexTest() {
		Integer[] array = {1, 2, 3, 4};
		assertEquals(3, getValueAtIndex(array, 2, 6));
		assertEquals(6, getValueAtIndex(array, 5, 6));
	}

	@Test
	void testConvert() {
		assertNull(hexToByteArray(null));
		assertThrows(IllegalArgumentException.class, () -> hexToByteArray("illegal"));
		assertArrayEquals(new byte[] { 0x01, 0x02 }, hexToByteArray("0x0102"));
		assertArrayEquals(new byte[] { (byte)0xab, (byte)0xcd, (byte)0xef }, hexToByteArray(" abCdEF    "));
		assertThrows(IllegalArgumentException.class, () -> hexToByteArray("123"));
		assertArrayEquals(new byte[] {}, hexToByteArray("   "));
	}

}

