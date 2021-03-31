package com.sentrysoftware.hardware.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.utils.Assert;

class AssertTest {

	private static final String MESSAGE = "message";
	private static final String VALUE = "value";

	@Test
	void testNotNullWithMessage() {

		try {
			Assert.notNull(VALUE, MESSAGE);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testNotNullWithMessageSupplier() {

		try {
			Assert.notNull(VALUE, () -> VALUE);
			Assert.notNull(VALUE, () -> null);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testNotNullWithMessageIllegalArgumentException() {

		try {
			Assert.notNull(null, MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals(MESSAGE, e.getMessage());
		}

	}

	@Test
	void testNotNullWithMessageSupplierIllegalArgumentException() {

		try {
			Assert.notNull(null, () -> MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals(MESSAGE, e.getMessage());
		}

		try {
			Assert.notNull(null, () -> null);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertNull(e.getMessage());
		}
	}

	@Test
	void testStateWithMessage() {

		try {
			Assert.state(VALUE != null, MESSAGE);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testStateWithMessageSupplier() {

		try {
			Assert.state(VALUE != null, () -> VALUE);
		} catch (Exception e) {
			fail("Expected no exception");
		}

		try {
			Assert.state(VALUE != null, () -> null);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testStateWithMessageIllegalStateException() {

		try {
			Assert.state(VALUE == null, MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			assertEquals(MESSAGE, e.getMessage());
		}
	}

	@Test
	void testStateWithMessageSupplierIllegalStateException() {

		try {
			Assert.state(VALUE == null, () -> MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			assertEquals(MESSAGE, e.getMessage());
		}

		try {
			Assert.state(VALUE == null, () -> null);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
			assertNull(e.getMessage());
		}
	}

	@Test
	void testIsTrueWithMessage() {

		try {
			Assert.isTrue(10 > 0, MESSAGE);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testIsTrueWithMessageSupplier() {

		try {
			Assert.isTrue(10 > 0, () -> VALUE);
			Assert.isTrue(10 > 0, () -> null);
		} catch (Exception e) {
			fail("Expected no exception");
		}
	}

	@Test
	void testIsTrueWithMessageIllegalArgumentException() {

		try {
			Assert.isTrue(10 < 0, MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals(MESSAGE, e.getMessage());
		}

	}

	@Test
	void testIsTrueWithMessageSupplierIllegalArgumentException() {

		try {
			Assert.isTrue(10 < 0, () -> MESSAGE);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals(MESSAGE, e.getMessage());
		}

		try {
			Assert.isTrue(10 < 0, () -> null);
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertNull(e.getMessage());
		}
	}
}
