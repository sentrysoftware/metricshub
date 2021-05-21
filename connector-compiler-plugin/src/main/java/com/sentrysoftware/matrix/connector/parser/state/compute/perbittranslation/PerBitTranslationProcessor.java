package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class PerBitTranslationProcessor extends AbstractStateParser {

	protected static final String PER_BIT_TRANSLATION_TYPE_VALUE = "PerBitTranslation";

	@Override
	public Class<PerBitTranslation> getType() {
		return PerBitTranslation.class;
	}

	@Override
	public String getTypeValue() {
		return PER_BIT_TRANSLATION_TYPE_VALUE;
	}
}
