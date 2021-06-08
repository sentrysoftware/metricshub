package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslateProcessorTest {

	@Test
	void testGetType() {

		assertEquals(ArrayTranslate.class, new TranslationTableProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(ArrayTranslateProcessor.ARRAY_TRANSLATE_TYPE_VALUE, new TranslationTableProcessor().getTypeValue());
	}
}