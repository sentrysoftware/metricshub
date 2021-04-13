package com.sentrysoftware.matrix.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CriterionTestResultTest {

	@Test
	void testEmpty() {
		assertFalse(CriterionTestResult.empty().isSuccess());
		assertNull(CriterionTestResult.empty().getMessage());
		assertNull(CriterionTestResult.empty().getResult());
	}

}
