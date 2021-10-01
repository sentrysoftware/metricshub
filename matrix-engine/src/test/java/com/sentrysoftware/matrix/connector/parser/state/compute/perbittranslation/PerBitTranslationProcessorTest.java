package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PerBitTranslationProcessorTest {

	@Test
	void testGetType() {

		assertEquals(PerBitTranslation.class, new BitListProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(PerBitTranslationProcessor.PER_BIT_TRANSLATION_TYPE_VALUE, new BitListProcessor().getTypeValue());
	}
}