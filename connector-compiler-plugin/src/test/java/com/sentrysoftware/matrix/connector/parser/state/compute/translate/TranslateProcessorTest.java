package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TranslateProcessorTest {

	@Test
	void testGetType() {

		assertEquals(Translate.class, new TranslationTableProcessor().getType());
	}

	@Test
	void testGetTypeValue() {

		assertEquals(TranslateProcessor.TRANSLATE_TYPE_VALUE, new TranslationTableProcessor().getTypeValue());
	}
}