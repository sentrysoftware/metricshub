package com.sentrysoftware.matrix.connector.model.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ConversionTypeTest {

	@Test
	void testGetByName() {
		assertEquals(ConversionType.ARRAY_2_SIMPLE_STATUS, ConversionType.getByName("Array2SimpleStatus"));
		assertEquals(ConversionType.ARRAY_2_SIMPLE_STATUS, ConversionType.getByName("array2simpleStatus"));
		assertEquals(ConversionType.HEX_2_DEC, ConversionType.getByName("hex2dec"));
		assertEquals(ConversionType.HEX_2_DEC, ConversionType.getByName("Hex2Dec"));
		assertThrows(IllegalArgumentException.class, () -> ConversionType.getByName(null));
		assertThrows(IllegalArgumentException.class, () -> ConversionType.getByName("otherConversionType"));
	}

}
