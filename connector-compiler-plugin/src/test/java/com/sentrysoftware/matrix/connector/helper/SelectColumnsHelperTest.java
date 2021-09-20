package com.sentrysoftware.matrix.connector.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

class SelectColumnsHelperTest {

	@Test
	void testCheckValue() {
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue(""));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue(" "));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue("1x"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue("-1x"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue("1x-"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue("1-x"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.checkValue("1 - 2"));

		assertEquals("3",  SelectColumnsHelper.checkValue(" 3 "));
		assertEquals("-3",  SelectColumnsHelper.checkValue(" -3 "));
		assertEquals("3-",  SelectColumnsHelper.checkValue(" 3- "));
		assertEquals("1-3",  SelectColumnsHelper.checkValue("  1-3"));
	}

	@Test
	void testConvertToList() {
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(null));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(" 1, 2, -4 "));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(" 1, 2-4, -5"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(" 1, 2, -4, 5 "));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(" 1, 2, 4- ,5 "));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList("4- ,5 , 6"));
		assertThrows(IllegalArgumentException.class, () -> SelectColumnsHelper.convertToList(" 4- ,5-6 , 7"));
		
		assertEquals(Collections.emptyList(), SelectColumnsHelper.convertToList(""));
		assertEquals(List.of("3"), SelectColumnsHelper.convertToList("3 "));
		assertEquals(List.of("-3", "5"), SelectColumnsHelper.convertToList(" -3, 5"));
		assertEquals(List.of("1", "3-"), SelectColumnsHelper.convertToList("1 , 3- "));
		assertEquals(List.of("1", "3-5", "7-"), SelectColumnsHelper.convertToList("1 , 3-5 , 7- "));
	}
}
