package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class TranslateProcessor extends AbstractComputeParser {

	protected static final String TRANSLATE_TYPE_VALUE = "Translate";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<Translate> getComputeType() {
		return Translate.class;
	}

	@Override
	public String getTypeValue() {
		return TRANSLATE_TYPE_VALUE;
	}
}
