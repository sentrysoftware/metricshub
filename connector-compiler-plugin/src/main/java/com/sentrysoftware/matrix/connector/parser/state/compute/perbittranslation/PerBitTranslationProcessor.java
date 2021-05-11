package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class PerBitTranslationProcessor extends AbstractComputeParser {

	protected static final String PER_BIT_TRANSLATION_TYPE_VALUE = "PerBitTranslation";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<PerBitTranslation> getComputeType() {
		return PerBitTranslation.class;
	}

	@Override
	public String getTypeValue() {
		return PER_BIT_TRANSLATION_TYPE_VALUE;
	}
}
